### 背景介绍

在这个教程中，我们将学习如何使用长短期记忆(long-short term memory, LSTM)神经网络来处理医学时间序列问题。这次使用的数据是来自4000名重症监护病房（ICU）患者，目标则是通过6个一般描述性特征(如年龄，性别，体重之类的)和37个序列特征值(如胆固醇，体温，pH，葡萄糖)来预测一个重症患者的死亡率。每一个患者都在不同的时间进行了多次不同的测量。LSTM对处理此类序列数据问题十分拿手，它可以避免一般的循环神经网络容易出现的梯度爆炸与梯度消失的问题，能够有效地捕获由于其细胞状态导致的长期依赖性。如果想要了解更多关于LSTM的知识，可以参考[官网](https://deeplearning4j.org/lstm.html)。

### 引入类
```
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
```
我们已经引入了所需要的类，接下来就需要下载数据并且转换成神经网络能够理解的数据对象。

### 数据源

下载的数据为`tar.gz`格式的压缩包，我们需要从下列url中下载数据，然后提取压缩包里边的csv文件。每一位患者都有一个独立的csv文件，其中sequence文件夹里的csv数据记录的是患者的各种特征值(features, 即输入值)，其中列表示各种特征值数据，而行数表示不同的时间步长(time step)。而mortality文件夹里的csv文件则是记录了这个患者的死亡结果，1表示存活了，0则相反。这个数据用作标签值(labels，即输出值)。
```
String DATA_URL = "https://skymindacademy.blob.core.windows.net/physionet2012/physionet2012.tar.gz";
String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_physionet2012").toString();
```

### 下载数据

我们将下载好的数据放入到一个临时文件夹中，还要指定一个路径用于解压文件。
```
// 如果没有数据就下载一个数据包到本地文件
public static boolean downloadFile(String remoteUrl, String localPath) throws IOException {
	boolean downloaded = false;
	if (remoteUrl == null || localPath == null) {
		return downloaded;
	}
	Path file = Paths.get(localPath);
	if (Files.notExists(file, LinkOption.NOFOLLOW_LINKS)) {
		Files.createDirectory(file.getParent());

		CloseableHttpClient client = HttpClients.createDefault();
		try (CloseableHttpResponse resp = client.execute(new HttpGet(remoteUrl))) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {
					entity.writeTo(output);
					output.flush();
				}
			}
		}
		downloaded = true;
	}
	if (Files.notExists(file, LinkOption.NOFOLLOW_LINKS)) {
		throw new IOException("File doesn't exist: " + localPath);
	}
	return downloaded;
}

String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_physionet2012").toString();
String localFilePath = Paths.get(DATA_PATH, "physionet2012.tar.gz").toString();
if (downloadFile(DATA_URL, localFilePath)) {
    System.out.println("download file from: " + DATA_URL);
}
```
接下来，解压文件:
```
// 解压"tar.gz"文件到指定文件夹
public static void extractTarGz(String inputPath, String outputPath) throws IOException {
	if (inputPath == null || outputPath == null)
		return;
	final int bufferSize = 4096;
	if (!outputPath.endsWith("" + File.separatorChar))
		outputPath = outputPath + File.separatorChar;
	try (TarArchiveInputStream tais = new TarArchiveInputStream(
			new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(inputPath))))) {
		TarArchiveEntry entry;
		while ((entry = (TarArchiveEntry) tais.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				new File(outputPath + entry.getName()).mkdirs();
			} else {
				int count;
				byte data[] = new byte[bufferSize];
				FileOutputStream fos = new FileOutputStream(outputPath + entry.getName());
				BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
				while ((count = tais.read(data, 0, bufferSize)) != -1) {
					dest.write(data, 0, count);
				}
				dest.close();
			}
		}
	}
}

Path dataPath = Paths.get(DATA_PATH, "physionet2012");
if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
    extractTarGz(localFilePath, DATA_PATH);
}
```

### 数据集迭代器

我们接下来是将csv中的原始数据转换成神经网络可以识别的数据集迭代器。我们需要建立一个包含了3200个样本的训练数据集迭代器，以及一个包含了800个样本的测试数据集迭代器。
```
//训练数据集大小
int NB_TRAIN_EXAMPLES = 3200;
//测试数据集大小
int NB_TEST_EXAMPLES = 800;
```
为了能够得到数据集迭代器，首先我们需要指定特征/标签数据的目录，然后实例化一个可以解析原始数据成类似记录数据格式的`CSVSequenceRecordReader`。
接下来我们使用上边创建好的`CSVSequenceRecordReader`生成一个`SequenceRecordReaderDataSetIterator`。我们这里使用的对齐方式是尾对齐(ALIGN_END)。这里是因为死亡率标签值始终位于序列的末尾，因此我们需要使用尾对齐的方式对齐所有序列，这样读取所有患者数据的最后一步就是死亡率了。想要了解更多关于对齐方式的知识，可以参考[官网](https://deeplearning4j.org/usingrnns)。
```
// 配置数据集
// 特征(输入)数据集路径
String featureBaseDir = Paths.get(dataPath.toString(), "sequence").toString();
// 标签(输出)数据集路径
String mortalityBaseDir  = Paths.get(dataPath.toString(), "mortality").toString();

//跳过第一行，因为第一行是表头。默认的分隔符就为","
SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(1, ",");
trainFeatures.initialize(
        new NumberedFileInputSplit(featureBaseDir + "/%d.csv",
                0, NB_TRAIN_EXAMPLES - 1));

SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
trainLabels.initialize(
        new NumberedFileInputSplit(mortalityBaseDir + "/%d.csv",
                0, NB_TRAIN_EXAMPLES - 1));

DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels,
        32, 2, false,
        SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

//测试数据
SequenceRecordReader testFeatures = new CSVSequenceRecordReader(1, ",");
testFeatures.initialize(
        new NumberedFileInputSplit(featureBaseDir + "/%d.csv",
                NB_TRAIN_EXAMPLES, NB_TRAIN_EXAMPLES + NB_TEST_EXAMPLES - 1));

SequenceRecordReader testLabels = new CSVSequenceRecordReader();
testLabels.initialize(
        new NumberedFileInputSplit(mortalityBaseDir + "/%d.csv",
                NB_TRAIN_EXAMPLES, NB_TRAIN_EXAMPLES + NB_TEST_EXAMPLES - 1));

DataSetIterator testData = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels,
        32, 2, false,
        SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);
```

### 神经网络配置

接下来我们使用DL4J中的`ComputationGraph`来解决上边描述的问题。
```
int NB_INPUTS = 86;
double LEARNING_RATE = 0.005;
int LSTM_LAYER_SIZE = 200;
int NUM_LABEL_CLASSES = 2;

ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(1234)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .weightInit(WeightInit.XAVIER)
        .updater(new Adam(LEARNING_RATE))
        .l2(0.01)
        .graphBuilder()
        .addInputs("trainFeatures")
        .setOutputs("predictMortality")
        .addLayer("L1", new LSTM.Builder()
                .nIn(NB_INPUTS).nOut(LSTM_LAYER_SIZE)
                .forgetGateBiasInit(1).activation(Activation.RELU)
                .build(), "trainFeatures")
        .addLayer("predictMortality", new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT)
                .nIn(LSTM_LAYER_SIZE).nOut(NUM_LABEL_CLASSES)
                .activation(Activation.SOFTMAX).build(), "L1")
        .pretrain(false).backprop(true)
        .build();

ComputationGraph model = new ComputationGraph(conf);
model.init();
```

### 模型训练

为了训练神经网络，我们只需在`for`循环中调用`ComputationGraph`的`fit`方法。
```
DataSetPreProcessor proc = new LastStepPreProc();
for (int i = 1; i <= 2; i++) {
    model.fit(trainData);
    trainData.reset();
}
```

### 评估模型

最后，我们可以使用ROC曲线下方的面积（Area under the Curve of ROC (AUC ROC)）评估模型。随机猜测模型的AUC接近0.50，而完美模型的AUC为1.00
```
ROC roc = new ROC(100);
while(testData.hasNext()){
    DataSet batch = testData.next();
    INDArray[] output = model.output(batch.getFeatures());
    roc.evalTimeSeries(batch.getLabels(), output[0]);
}
System.out.println("FINAL TEST AUC: " + roc.calculateAUC());
```
我们将看到这个模型经过测试数据集评估后的AUC值为0.69！