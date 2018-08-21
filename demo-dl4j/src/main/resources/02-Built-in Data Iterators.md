### 背景情况

对于任何的神经网络入门教程来说，玩具数据集都是十分重要的。Deeplearning4j内置了一些常见的数据集，包括但不限于：

* MNIST
* Iris
* TinyImageNet (subset of ImageNet)
* CIFAR-10
* Labelled Faces in the Wild
* Curve Fragment Ground-Truth Dataset

这些数据集也是经常作用于测试其他的机器学习算法的可靠度。需要注意的是有些数据集有它们的使用许可(例如想在商业项目中使用ImageNet的话就需要获得它的专门许可)。

### 在这个教程里将学到的知识

在我们认识了`MultiLayerNetwork`及`ComputationGraph`之后，我们将生成一些数据迭代器，用于将玩具数据集交给神经网络训练。这个教程将着重于如何训练一个分类器(你也可以训练一个回归网络，及一些非监督学习例如自编码)，还能学到了解控制台输出的数据。

### 引入类

```
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
```

### MNIST分类器神经网络

`MultiLayerNetwork`可以识别MNIST数据集中的手写数字。如果你对MNIST不怎么熟悉，这里简要说明一下：MNIST由许多张手写数字图片组成，他们都是28*28像素大小的黑白图片，包含了0-9个手写数字。想了解更多关于MNIST的知识，可以参考[这里](https://en.wikipedia.org/wiki/MNIST_database)。

当你引入了必须的类，就可以开始生成一个基本的`MultiLayerNetwork`:
```
//图片高度及宽度
int numRows = 28;
int numColumns = 28;
//最终输出分类数
int outputNum = 10;
//每个批次载入的数据量
int batchSize = 128;
//随机权重初始值
int rngSeed = 123;
//运行批次
int numEpochs = 15;

MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
		.seed(rngSeed)
		.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		.updater(new Nesterovs(0.006, 0.9))
		.l2(1e-4)
		.list()
		.layer(0, new DenseLayer.Builder()
			.nIn(numRows * numColumns)
			.nOut(1000)
			.activation(Activation.RELU)
			.weightInit(WeightInit.XAVIER)
			.build())
.layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
			.nIn(1000)
			.nOut(outputNum)
			.activation(Activation.SOFTMAX)
			.weightInit(WeightInit.XAVIER)
			.build())
		.build();

MultiLayerNetwork model = new MultiLayerNetwork(conf);
model.init();
model.setListeners(new ScoreIterationListener(1));
```

### 使用MNIST数据迭代器

MNIST数据迭代器跟DeepLearning4j其他的内置数据迭代器一样继承于`DataSetIterator`。它的构造函数将会自动下载MNIST数据到用户文件夹下然后再初始化成`DataSetIterator`子类。同时还可以通过构造函数指定生成的数据迭代器是训练数据集还是测试数据集。这里我们将生成两个不同的数据迭代器:
```
DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);
```

### 执行基本训练

现在网络模型已经配置好了，训练测试数据也生成好了，就差开始训练了。而训练则需要几句代码就可以执行。
```
for (int i = 0; i <= numEpochs; i++) {
	model.fit(mnistTrain);
}
```
之前我们通过`setListeners()`方法将`ScoreIterationListener`设置到模型中，我们可以在控制台观察输出的分数。随着精心调校的模型持续的训练，其错误分数将随着每次迭代而降低。此错误/损失分数最终会逐渐接近零。需要注意的是更复杂的网络和问题可能永远不会产生最佳分数。如果您想要成为这方面的专家这些就需要你继续调整和更改模型配置。

### 评估模型

过拟合是深度学习中常见的问题，它主要表现在神经网络模型不能很好的概况你需要解决的问题。如果您在训练数据集上运行了太多次的深度学习算法，却没有使用Dropout等正则化技术，或者训练数据集不够大到能囊括在您在现实世界中需要解释的所有分类，就会发生这种情况。

Deeplearning4j带有一些评估网络模型的内置工具。最简单的是将测试数据迭代器给`eval()`从而获得一个`Evaluation`对象。[`org.deeplearning4j.eval`](https://github.com/deeplearning4j/deeplearning4j/tree/master/deeplearning4j-core/src/test/java/org/deeplearning4j/eval)包中提供了更多的评估器，包括ROC绘图和回归评估。

```
Evaluation eval = model.evaluate(mnistTest);
System.out.println("Accuracy: " + eval.accuracy());
System.out.println("Precision: " + eval.precision());
System.out.println("Recall: " + eval.recall());
System.out.println(eval.confusionToString());
```