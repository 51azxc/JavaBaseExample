### 背景介绍

为什么要用自编码？实际上，自动编码器通常用于数据去噪和降维。这对于表现学习非常有用，而对于数据压缩来说则不那么好。

在深度学习中，自编码是一种“尝试”重构其输入的神经网络。它可以作为一种特征提取形式，然后可以不断的叠加以建立“深层”网络（参见[DeepBelief](https://deeplearning4j.org/deepbeliefnetwork.html)等网络）。而由自编码产生的特征可以被馈送到其他分类，聚类和异常检测的算法中。

当原始输入数据具有高维度且无法轻松绘制时，自编码对数据可视化也很有用。通过降低维度，输出值可以压缩到二维或3维空间，以便更好地进行数据探索。

数据去噪

![Data Denoising ](https://upload.wikimedia.org/wikipedia/commons/d/d8/TVD_1D_Example.png)

降维

![Dimensionality Reduction](https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Kernel_Machine.svg/512px-Kernel_Machine.svg.png)

### 自编码是怎么工作的？

自编码由编码函数(encoder)、解码函数(decoder)及损失率函数(loss function)组成。输入数据经过自编码的处理后会压缩成特有的特征。然后解码器将会学习如何从压缩后的特征集中重构成原始输入数据。在这个无监督学习过程中，损失函数则会帮助校正解码器产生的错误输出。整个过程都是自动的（要不咋叫自(动)编码(器)呢），不需要人为干预。

### 本教程的主要知识点

之前你已经知道如何用`MultiLayerNetwork`及`ComputationGraph`构建不同的神经网络配置，我们这里将会创建一个"堆叠(stacked)"式自动编码器,在没有预训练的情况下对MNIST数字执行异常检测。我们的目标是识别一些写的比较特别(特难看)的数字。从给定数据集的标记或发现“特别”的数据被广泛地称为*异常检测(anomaly detection)*。异常检测不需要标签(labels)数据集,它可以在无监督学习的情况下进行，这对于世界上大多数数据都没有标记的情况下是很有帮助的。

异常检测使用重建误差来测量解码器的执行情况。原型示例有较低的重构误差，而异型示例则由较高的重构误差。

#### 哪些情况使用异常检测比较好？

应用异常检测的示例有：网络入侵，欺诈检测，系统监控，检查传感器网络事件（IoT）和检测异常轨迹传感器。

```
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
```

### 堆叠式自动编码器

以下自动编码器使用两个堆叠的密集层进行编码。MNIST数字被转换为长度为784的一维数组（MNIST图像为28x28像素，因此将它们转换需要的大小为784）。
784 → 250 → 10 → 250 → 784

```
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(12345)
        .weightInit(WeightInit.XAVIER)
        .updater(new AdaGrad(0.05))
        .activation(Activation.RELU)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .l2(0.0001)
        .list()
        .layer(0, new DenseLayer.Builder().nIn(784).nOut(250).build())
        .layer(1, new DenseLayer.Builder().nIn(250).nOut(10).build())
        .layer(2, new DenseLayer.Builder().nIn(10).nOut(250).build())
        .layer(3, new OutputLayer.Builder().nIn(250).nOut(784)
    			.lossFunction(LossFunction.MSE)
    			.build())
        .pretrain(false).backprop(true)
        .build();
MultiLayerNetwork model = new MultiLayerNetwork(conf);
model.init();
model.setListeners(new ScoreIterationListener(1));
```

### 使用MNIST迭代器

与大多数Deeplearning4j的内置迭代器一样，MNIST迭代器继承于`DataSetIterator`类。,因此它支持从后台自动的下载数据到本地然后将其胜利后成对应的数据集。
```
//载入50000条数据并且将其分成40000条训练数据，10000条测试数据
DataSetIterator iter = new MnistDataSetIterator(100,50000,false);

List<INDArray> featuresTrain = new ArrayList<>();
List<INDArray> featuresTest = new ArrayList<>();
List<INDArray> labelsTest  = new ArrayList<>();

Random random = new Random(12345);

while (iter.hasNext()) {
	DataSet next = iter.next();
	//从100条数据中分割成按80/20的比例分割
	SplitTestAndTrain split = next.splitTestAndTrain(80, random);
	featuresTrain.add(split.getTrain().getFeatures());
	featuresTest.add(split.getTest().getFeatures());
	//将单热编码转换成数据对应的索引值
	//例如[0,0,0,1,0,0,0,0,0,0]，这里唯一不为0的数值索引为4，因此将这条数据转换成4
	labelsTest.add(Nd4j.argMax(split.getTest().getLabels(), 1));
}
```

### 非监督学习

现在整个神经网络已经配置好了，MNIST训练/测试数据集也准备好了，只需要几行代码，就可以开始训练了。
早先，我们通过`setListeners()`将`ScoreIterationListener`附加到我们的模型中，这样可以在控制台看到不断输出的损失分数。
```
for (int i = 1; i <= 30; i++) {
	for (INDArray data: featuresTrain) {
		model.fit(data, data);
	}
	System.out.println("Epoch " + i + " complete");
}
```

### 评估模型

现在自动编码器已经训练完毕，我们将利用测试数据来评估模型。每个示例将被单独评分，然后用一个`map`将数字与(得分，示例)元组列表保存起来。最终将会取出前N个最好的数据以及前N个最差的数据。
```
Map<Integer, List<Pair<Double, INDArray>>> listsByDigit = new HashMap<>();
for (int i = 0; i <= 9; i++) {
	listsByDigit.put(i, new ArrayList<Pair<Double, INDArray>>());
}

//通过测试数据来评估模型准确度
for (int i = 0; i <= featuresTest.size() - 1; i++) {
	INDArray testData = featuresTest.get(i);
	INDArray labels = labelsTest.get(i);
	for (int j = 0; j <= testData.rows() - 1; j++) {
		INDArray example = testData.getRow(j);
		int digit = new Double(labels.getDouble(j)).intValue();
		double score = model.score(new DataSet(example, example));
		List<Pair<Double, INDArray>> digitAllPairs = listsByDigit.get(digit);
		digitAllPairs.add(new ImmutablePair<Double, INDArray>(score, example));
	}
}

Comparator<Pair<Double, INDArray>> c = new Comparator<Pair<Double,INDArray>>() {
	@Override
	public int compare(Pair<Double, INDArray> o1, Pair<Double, INDArray> o2) {
		return java.lang.Double.compare(o1.getLeft(), o2.getLeft());
	}
};

for (List<Pair<Double, INDArray>> digitAllPairs: listsByDigit.values()) {
	Collections.sort(digitAllPairs, c);
}

List<INDArray> best = new ArrayList<>(50);
List<INDArray> worst = new ArrayList<>(50);

for (int i = 0; i <= 9; i++) {
	List<Pair<Double, INDArray>> digitAllPairs = listsByDigit.get(i);
	for (int j = 0; j <= 4; j++) {
		best.add(digitAllPairs.get(j).getRight());
		worst.add(digitAllPairs.get(digitAllPairs.size() - 1 - j).getRight());
	}
}
```

### 利用BufferedImage显示数据

通过利用Base64编码技术，我们可以将输出的INDArray转换成图片并且显示出来。
```
public static String encodeArrayToImage(INDArray arr) throws IOException {
	BufferedImage bi = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
	for (int i = 0; i < 784; i++) {
		bi.getRaster().setSample(i % 28,  i / 28, 0, new Double(255*arr.getDouble(i)).intValue());
	}
	
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	
	ImageIO.write(bi, "PNG", baos);
	byte[] image = baos.toByteArray();
	baos.close();
	
	return Base64.getEncoder().encodeToString(image);
}
```
最终打印成html元素放到网页中显示出来：
```
System.out.println("<br /><h2>Worst Scoring Digits</h2>");
worst.stream().map(s -> {
	try {
		String result = "<img src=\"data:image/png;base64," + encodeArrayToImage(s) +
				"\" style=\"float:left; display:block; margin:10px\">";
		return result;
	} catch (IOException e) {
		e.printStackTrace();
	}
	return null;
}).forEach(System.out::println);

System.out.println("<br /><h2>Best Scoring Digits</h2>");
best.stream().map(s -> {
	try {
		String result = "<img src=\"data:image/png;base64," + encodeArrayToImage(s) +
				"\" style=\"float:left; display:block; margin:10px\">";
		return result;
	} catch (IOException e) {
		e.printStackTrace();
	}
	return null;
}).forEach(System.out::println);
```