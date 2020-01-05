### 背景介绍

这个教程与前边那个教程十分相像，唯一不同的是我们不会使用多任务来训练我们的神经网络。我们使用的数据最初来自于[Kaggle挑战](https://kaggle.com/c/instacart-marke-basket-analysis)。我们首先删除了使用instacart应用程序只生成1个订单的用户，然后从剩下数据中挑选了5000名用户作为了本教程的数据。

我们可以从每个订单中获取有关用户购买的产品的信息。例如产品名称，所在的过道及他们所属的类别区域。为了构建特征，我们将从每个订单中提取用户是否在指定过道中购买产品作为指标，总共由134个过道。目标是用户是否会在下一个订单中去早餐食品区域购买产品。我们不会使用任何辅助目标。

由于数据中的时间依赖性，我们将搭建一个基于LSTM的模型。

### 引入类
```
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
```

### 下载数据
```
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

String DATA_URL = "https://bpstore1.blob.core.windows.net/tutorials/instacart.tar.gz";
String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_instacart").toString();
String localFilePath = Paths.get(DATA_PATH, "instacart.tar.gz").toString();
if (DataUtilities.downloadFile(DATA_URL, localFilePath)) {
   System.out.println("download file from: " + DATA_URL);
}
```
接下来，解压数据文件:
```
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

Path dataPath = Paths.get(DATA_PATH, "instacart");
if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
    DataUtilities.extractTarGz(localFilePath, DATA_PATH);
}
```

### 数据集迭代器

接下来，我们需要将csv文件中的原始数据加载到神经网络能识别的数据集迭代器中。我们将设置包含4000个用例的训练数据集及包含1000个用例测试数据集。
```
String featureBaseDir = Paths.get(dataPath.toString(), "features").toString();
String targetsBaseDir = Paths.get(dataPath.toString(), "breakfast").toString();
```
我们首先实例化一个可以解析原始数据成类似记录数据格式的`CSVSequenceRecordReader`。接下来使用`RecordReader`创建`SequenceRecordReaderDataSetIterator`。由于给出的数据长短各不相同，因此需要使用`ALIGN_END`对齐模式。
```
SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(1);
trainFeatures.initialize(
        new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 1, 4000));
SequenceRecordReader trainLabels = new CSVSequenceRecordReader(1);
trainLabels.initialize(
        new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 1, 4000));
DataSetIterator train = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, 32,
        2, false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

SequenceRecordReader testFeatures = new CSVSequenceRecordReader(1);
testFeatures.initialize(
        new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 4001, 5000));
SequenceRecordReader testLabels = new CSVSequenceRecordReader(1);
testLabels.initialize(
        new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 4001, 5000));
DataSetIterator test = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels, 32,
        2, false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);
```

### 神经网络

下一个任务是设置神经网络配置类。与之前的教程大部分类似，不过这里将使用一个LSTM层及一个`RnnOutputLayer`层。
```
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).seed(12345)
    .weightInit(WeightInit.XAVIER)
    .updater(new Adam())
    .list()
    .layer(0, new LSTM.Builder()
            .activation(Activation.TANH)
            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
            .gradientNormalizationThreshold(10)
            .nIn(134)
            .nOut(150)
            .build())
    .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT)
            .activation(Activation.SOFTMAX)
            .nIn(150)
            .nOut(2)
            .build())
    .pretrain(false).backprop(true)
    .build();
```
我们必须初始化神经网络。
```
MultiLayerNetwork model = new MultiLayerNetwork(conf);
model.init();
```

### 训练模型

我们将训练5个批次:
```
for (int i = 1; i <= 5; i++) {
    System.out.println("Epoch " + i);
    model.fit(train);
    train.reset();
}
```

### 评估模型

我们现在将评估我们训练的模型。我们将使用ROC曲线的曲线下面积（AUC）度量。
```
ROC roc = new ROC(100);
test.reset();

while(test.hasNext()){
    DataSet next = test.next();
    INDArray features =  next.getFeatures();
    INDArray output = model.output(features);
    roc.evalTimeSeries(next.getLabels(), output);
}

System.out.println(roc.calculateAUC());
```
我们最终的带了0.64的AUC值!