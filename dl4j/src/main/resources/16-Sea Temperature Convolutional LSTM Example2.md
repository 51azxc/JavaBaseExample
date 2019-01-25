### 背景介绍

在本教程中与上个教程类似，我们将创建一个神经网络来预测海水日常温度。回想一下，我们使用的数据是从[地球系统研究实验室](https://www.esrl.noaa.gov/psd/)得到的孟加拉，韩国，黑色，地中海，阿拉伯，日本，渤海和鄂霍次克海8个海洋1981-2017年的温度数据。它们保存成csv文件，每个示例由50个二维表格组成，因此每个csv文件都具有50行数据。

对于此任务，我们将使用卷积LSTM神经网络来预测在给定的温度网格序列之后10天的海水温度。这次的训练模型与上一个教程类似，但是评估的方式不会相同（仅应用于序列之后的10天）。


### 引入类
```
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.preprocessor.RnnToCnnPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.CnnToRnnPreProcessor;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.eval.RegressionEvaluation;

import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
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

String DATA_URL = "https://bpstore1.blob.core.windows.net/seatemp/sea_temp2.tar.gz";
String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_seas2").toString();
String localFilePath = Paths.get(DATA_PATH, "sea_temp.tar.gz").toString();
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

Path dataPath = Paths.get(DATA_PATH, "sea_temp");
if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
    DataUtilities.extractTarGz(localFilePath, DATA_PATH);
}
```

### 数据集迭代器

接下来，我们需要将csv文件中的原始数据加载到神经网络能识别的数据集迭代器中。我们将设置包含1600个用例的训练数据集及包含136个用例的测试数据集。训练和测试数据中序列之后10天的温度也将包含在单独的DataSetIterator中。
```
String featureBaseDir = Paths.get(dataPath.toString(), "features").toString();
String targetsBaseDir = Paths.get(dataPath.toString(), "targets").toString();
String futureBaseDir = Paths.get(dataPath.toString(), "futures").toString();
```
我们首先实例化一个可以解析原始数据成类似记录数据格式的`CSVSequenceRecordReader`。
接下来我们使用上边创建好的`CSVSequenceRecordReader`生成一个`SequenceRecordReaderDataSetIterator`。由于每个示例具有50个时间步长，因此需要相等长度的对齐模式。另请注意，这是基于回归的任务，而不是分类任务。
```
int numSkipLines = 1;
boolean regression = true;
int batchSize = 32;

//训练数据
SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(numSkipLines);
trainFeatures.initialize(
        new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 1, 1600));
SequenceRecordReader trainTargets = new CSVSequenceRecordReader(numSkipLines);
trainTargets.initialize(
        new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 1, 1600));

DataSetIterator train = new SequenceRecordReaderDataSetIterator(trainFeatures, trainTargets, batchSize,
        10, regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);

//测试数据
SequenceRecordReader testFeatures = new CSVSequenceRecordReader(numSkipLines);
testFeatures.initialize(
        new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 1601, 1736));
SequenceRecordReader testTargets = new CSVSequenceRecordReader(numSkipLines);
testTargets.initialize(
        new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 1601, 1736));

DataSetIterator test = new SequenceRecordReaderDataSetIterator(testFeatures, testTargets, batchSize,
        10, regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);

SequenceRecordReader futureFeatures = new CSVSequenceRecordReader(numSkipLines);
futureFeatures.initialize(
		new NumberedFileInputSplit(futureBaseDir + "/%d.csv", 1601, 1736));
SequenceRecordReader futureLabels = new CSVSequenceRecordReader(numSkipLines);
futureLabels.initialize(
		new NumberedFileInputSplit(futureBaseDir + "/%d.csv", 1601, 1736));
DataSetIterator future = new SequenceRecordReaderDataSetIterator(futureFeatures, futureLabels, batchSize, 10,
		regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);

```

### 神经网络

下一个任务是初始化卷积LSTM神经网络的参数，然后设置神经网络配置类。
```
int V_HEIGHT = 13;
int V_WIDTH = 4;
int kernelSize = 2;
int numChannels = 1;
```
在生成的配置中，我们将配置一个卷积层，一个最大池化层，一个LSTM层及一个输出层。为了适配卷积层的输入，我们将通过`RnnToCnnPreProcessor`将输入的三维数据[批量大小，高度x网格宽度，时间序列长度]变成(reshape)四维数据[示例中的x个时间序列长度，通道，宽度，高度]。之后需要使用`CnnToRnnPreProcessor`将该卷积形状转换回原始的3维形状。
```
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).seed(12345)
	.updater(new Adam()).weightInit(WeightInit.XAVIER).list()
	.layer(0, new ConvolutionLayer.Builder(kernelSize, kernelSize).updater(new AdaGrad()).nIn(numChannels)
			.nOut(7).stride(2, 2).activation(Activation.RELU).build())
	.layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
	        .kernelSize(kernelSize, kernelSize)
	        .stride(2, 2).build())
	    .layer(2, new LSTM.Builder()
	        .activation(Activation.SOFTSIGN)
	        .nIn(21)
	        .nOut(100)
	        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
	        .gradientNormalizationThreshold(10)
	        .build())
	    .layer(3, new RnnOutputLayer.Builder(LossFunction.MSE)
	        .activation(Activation.IDENTITY)
	        .nIn(100)
	        .nOut(52)
	        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
	        .gradientNormalizationThreshold(10)
	        .build())
	    .inputPreProcessor(0, new RnnToCnnPreProcessor(V_HEIGHT, V_WIDTH, numChannels))
	    .inputPreProcessor(2, new CnnToRnnPreProcessor(3, 1, 7 ))
	    .pretrain(false).backprop(true)
	    .build();

MultiLayerNetwork model = new MultiLayerNetwork(conf);
model.init();
```

### 训练模型

我们将训练15个批次:
```
for (int i = 1; i <= 15; i++) {
	System.out.println("Epoch " + i);
	model.fit(train);
	train.reset();
}
```

### 评估模型

我们现在将评估我们训练的模型。我们将使用`RegressionEvaluation`，因为我们的任务是回归而不是分类任务。我们将仅使用给定的每日温度序列后10天的温度而不是序列中每天的温度来评估模型。这里就需要使用`MultiLayerNetwork`的`rnnTimeStep()`方法来完成评估。
```
RegressionEvaluation eval = new RegressionEvaluation();
test.reset();
future.reset();
while(test.hasNext()) {
	DataSet next = test.next();
	INDArray features = next.getFeatures();
	INDArray pred = Nd4j.zeros(1, 2);
	
	for (int i = 0; i <= 49; i++) {
		pred = model.rnnTimeStep(features.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(i,i+1)));
	}
	
	DataSet correct = future.next();
	INDArray cFeatures = correct.getFeatures();
	
	for (int i = 0; i <= 9; i++) {
		eval.evalTimeSeries(pred, cFeatures.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(i,i+1)));
		pred = model.rnnTimeStep(pred);
	}
	model.rnnClearPreviousState();
}
```
最终打印出模型评估的统计数据:
```
System.out.println(eval.stats());
```