### 背景介绍

循环神经网络(Recurrent neural networks, RNN)对处理本质序列数据十分在行。循环网络与前馈网络的区别在于有一个反馈循环，将第n-1步的输出反馈给神经网络，对第n步的输出产生影响，之后的每一步也都以此类推。前馈网络仅会根据当前读取到的那一个样例中的像素来作出判断，而不会依据之前学习过的样例来调整预测结果。前馈网络一次只能接受一项输入，产生一项输出；循环网络则没有这种一对一的限制。

RNN也可以用于一些输入是序列数据而输出却不是数据集的情况。在这些情况下，RNN的最后一步(time step)将被视作整个输出值。对于分类器来说，最后一步将被视作输出的标签分类。

在这个教程中我们将会使用DL4J中的`MultiLayerNetwork`构建一个RNN分类器。

RNN也可以应用于输入是连续的但输出不连续的情况。 在这些情况下，RNN的最后一个时间步的输出通常被视为整个观察的输出。 对于分类，最后一个时间步的输出将是观察的预测类标签。

### 引入类
```
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
```

### 下载数据

UCI有一系列可与用于机器学习的数据集。请确保你的磁盘有足够的空间装载数据。我们这里使用的数据集是[UCI synthetic control](https://archive.ics.uci.edu/ml/datasets/synthetic+control+chart+time+series)。它有600行数据，而每隔100条数据则是一个分类。以下代码用于下载数据：
```
Path dataPath = Paths.get(System.getProperty("java.io.tmpdir"), "uci_synthetic_control");
System.out.println(dataPath.toString());
if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
	String url = "https://archive.ics.uci.edu/ml/machine-learning-databases/synthetic_control-mld/synthetic_control.data";
	System.out.println("Downloading file...");
	String data = IOUtils.toString(new URL(url), Charset.defaultCharset());
	String[] lines = data.split("\n");
	
	int lineCount = 0;
	int index = 0;
	
	List<String> linesList = new ArrayList<>();
	
	for (String line : lines) {
		Integer count = new Integer(lineCount / 100);
		String newLine = null;
		//文件默认是以空格分割数据的，现在讲分隔符变成", "，然后列转行
		newLine = line.replaceAll("\\s+", ", " + count.toString() + "\n");
		//结尾追加分割符与标签值
		newLine += ", " + count.toString();
		linesList.add(newLine);
		lineCount += 1;
	}

	//将数据随机打乱，不然都是统一的数据无法测试
	Collections.shuffle(linesList);
	
	for (String line : linesList) {
		//将数据写入到csv文件中
		Path outPath = Paths.get(dataPath.toString(), index + ".csv");
		FileUtils.writeStringToFile(outPath.toFile(), line, Charset.defaultCharset());
		index += 1;
	}
	System.out.println("Done");
} else {
	System.out.println("File already exists");
}
```
### 在硬盘中生成数据集迭代器

现在我们将下载好的数据集存入到csv格式的文件中。我们需要将它们转换成神经网络模型可读的`CSVSequenceRecordReader`对象，如果之前你已经保存好了数据，你就可以运行此代码块将数据处理成数据集：
```
int batchSize = 128;
//总共6个类别
int numLabelClasses = 6;

//训练数据
//指定分隔符为", "
CSVSequenceRecordReader trainRR = new CSVSequenceRecordReader(0, ", ");
//NumberedFileInputSplit可以通过占位符来读取一堆csv文件
trainRR.initialize(
		new NumberedFileInputSplit(dataPath.toAbsolutePath().toString() + "/%d.csv", 0, 449));
//指定标签索引为1
DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainRR, batchSize, numLabelClasses, 1);

//测试数据
CSVSequenceRecordReader testRR = new CSVSequenceRecordReader(0, ", ");
testRR.initialize(
		new NumberedFileInputSplit(dataPath.toAbsolutePath().toString() + "/%d.csv", 450, 599));
DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testRR, batchSize, numLabelClasses, 1);
```

### 神经网络配置

万事俱备只欠东风。我们将使用如下配置来构建我们的神经网络。因为有6个分类类别，我们需要将输出值设置为6。而我们已经将数据特征的列转换成了行，因此每个输入值都只有一个数据，所以输入值为1：
```
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
		.seed(123)
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.weightInit(WeightInit.XAVIER)
		//学习速率为0.005
		.updater(new Nesterovs(0.005, 0.9))
		.gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
		.gradientNormalizationThreshold(0.5)
		.list()
		.layer(0, new LSTM.Builder().activation(Activation.TANH)
				.nIn(1).nOut(600).build())
		.layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
				.activation(Activation.SOFTMAX).nIn(600).nOut(numLabelClasses).build())
		.pretrain(false).backprop(true).build();

MultiLayerNetwork model = new MultiLayerNetwork(conf);
model.setListeners(new ScoreIterationListener(20));
```

### 模型训练

我们可以指定训练的批次也就是循环数。运行`fit()`方法即可开始训练模型。
```
for (int i = 0; i < 10; i++) {
    model.fit(trainIter);
}
```

### 评估模型

训练完成后，我们只需几行代码就可以利用测试数据集来评估我们的模型。使用测试数据集来评估模型为了防止对训练数据的过拟合。如果对训练数据过拟合了，我们将会得到一些没那么有效的数据。
如果你想要提取混淆矩阵，可以使用`Evalution`的其他内置方法。通常里边还有其他可用的评估工具例如计算曲线下面积(AUC)。
```
Evaluation evaluation = model.evaluate(testIter);
System.out.println("Accuracy: " + evaluation.accuracy());
System.out.println("Precision: " + evaluation.precision());
System.out.println("Recall: " + evaluation.recall());
```