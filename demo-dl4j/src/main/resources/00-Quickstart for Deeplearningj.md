### Deeplearning4j入门

Deeplearning4j - 简称“DL4J” - 是一门创建多层深度神经网络的高性能特定语言。 ,Deeplearning4j是[开源](https://github.com/deeplearning4j/deeplearning4j/)的，由Eclipse Foundation及社区贡献者维护，编写语言包括C++，Java，Scala和Python。

### 手写数字识别

在本教程中，你将会使用Deeplearning4j创建并训练一个可以识别随机手写数字的深层神经网络。这些年来手写数字识别被各种机器学习的算法尝试过，Deeplearning的表现十分好，对MNIST数据集的识别率高达99.7%。我们将会使用[EMNIST](https://www.nist.gov/itl/iad/image-group/emnist-dataset)数据集来区分数字，它是"下一代"的MNIST大型数据集。

![mnist](https://deeplearning4j.org/img/mnist_render.png)

### 主要学习点

1. 加载数据集到神经网络模型。
2. 格式化EMNIST以进行图像识别。
3. 创建一个深层神经网络模型。
4. 训练模型。
5. 评估你的模型性能。

### 主要引入类

```
import org.deeplearning4j.datasets.iterator.impl.EmnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.ROCMultiClass;
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
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
```

### 准备载入的数据

数据集迭代器是代码中十分重要的一部分，他可以从数据集中按批次处理和迭代数据，以便让神经网络对数据进行训练和推断。Deeplearning4j带有一个`EmnistDataSetIterator`,它实现了`BaseDatasetIterator`接口，专门用于对EMNIST数据的下载及准备工作。
需要注意的是我们在下面创建了两个不同的数据迭代器：训练数据和测试数据。测试用于在训练后评估模型的准确率。构造函数中的最后一个`boolean`参数表示为将此数据迭代器实例化为训练还是测试数据。 如果你不想在本教程使用它，您可以在[ETL用户指南](https://deeplearning4j.org/etl-userguide)中了解更多加载神经网络数据的方式。DL4J带有许多数据记录读取器，可以很方便的从CSV，图像，视频，音频和序列中加载数据并将数据转换为ND阵列。

```
 //一次训练抓取的数据
int batchSize = 16;
EmnistDataSetIterator.Set emnistSet = EmnistDataSetIterator.Set.BALANCED;
DataSetIterator emnistTrain = new EmnistDataSetIterator(emnistSet, batchSize, true);
DataSetIterator emnistTest = new EmnistDataSetIterator(emnistSet, batchSize, false);
```

### 搭建神经网络

`NeuralNetConfiguration`[类](https://deeplearning4j.org/neuralnet-configuration.html)是在Deeplearning4j中搭建任何的神经网络模型的地基。我们用这个类来配置各项超参数，其数值决定了网络的架构和算法的学习方式。直观而言，每一项超参数就如同一道菜里的一种食材：取决于食材好坏，这道菜也许非常可口，也可能十分难吃……所幸在深度学习中，如果结果不正确，超参数还可以进行调整。
`list()`函数可指定网络中层的数量；它会将您的配置复制n次，建立分层的网络结构。
隐藏层中的每个节点（圆圈）表示MNIST数据集中手写数字的特征。例如，假设你正在查看数字6， 一个节点可以表示圆形边，另一个节点可以表示卷曲线的交集，依此类推。这些特征通过模型系数的权重加权，并在每个隐藏层中重新组合以帮助预测手写数字是否确实是6。你拥有的节点层数越多，它们对数据捕获的复杂性和细微差别就越大，这样他们就可以更好的做出预测。
![layers](https://deeplearning4j.org/img/onelayer_labeled.png)

你可以将某个层视为“隐藏”，因为虽然您可以看到输入进入网络，并且决策即将发布，但人类很难解读神经网络如何以及为何在内部处理数据。,神经网络模型的参数只是数字的长向量，可由机器读取。

当你看到输入值进入网络，然后产生了结果输出，而在这其中是神经网络将数据处理成简单的长向量数字。这对机器来说清晰明了，但是对人类来说就没那么容易明白了。因此你只需要将其想象成"隐藏层"即可。

```
//获取最终输出的分类数
int outputNum = EmnistDataSetIterator.numLabels(emnistSet);
int rngSeed = 123;
//图片的高度及宽度。也就是各自包含的像素点数
int numRows = 28;
int numColumns = 28;

MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    .seed(rngSeed)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .updater(new Adam())
    .l2(1e-4)
    .list()
    .layer(0, new DenseLayer.Builder()
        //输入的数据点数
        .nIn(numRows * numColumns)
        //输出数据点数
        .nOut(1000)
        //激活函数
        .activation(Activation.RELU)
        //权重值初始化
        .weightInit(WeightInit.XAVIER)
        .build())
    .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nIn(1000)
        .nOut(outputNum)
        .activation(Activation.SOFTMAX)
        .weightInit(WeightInit.XAVIER)
        .build())
    .build();
```

### 模型训练

现在我们已经构建了`NeuralNetConfiguration`，我们可以使用它来实例化`MultiLayerNetwork`。当我们调用网络模型network的`init()`方法时，它会初始化权重值，并且将传递数据给网络模型训练。如果我们想在训练期间看到损失率分数，我们可以给神经网络添加一个监听器。

一个网络模型的实例化对象拥有`fit()`方法，他接收的参数包括数据集迭代器(继承于`BaseDatasetIterator`),一个`DataSet`，或者一个ND-Array(实现了`INDArray`接口的类)。由于我们的EMNIST迭代器已经继承了数据集迭代器基类，因此我们可以直接将它传入到`fit()`方法中。如果想要多批次的训练，`fit()`方法支持接收第二个参数用于指定训练批次数

```
MultiLayerNetwork network = new MultiLayerNetwork(conf);
network.init();

//每隔5个迭代就输出一次训练分数
int eachIterations = 5;
network.addListeners(new ScoreIterationListener(eachIterations));

//单次训练
network.fit(emnistTrain);

//多次训练
//network.fit(emnistTrain, 2);
```

### 模型评估

Deeplearning4j拥有多种工具来[评估](https://deeplearning4j.org/evaluation)模型性能。你可以获得一些基本的评估指标例如准确度和精确率，或者是用一个操作特征接收者操作特征(receiver operating characteristic, [ROC](https://zh.wikipedia.org/wiki/ROC%E6%9B%B2%E7%BA%BF))。需要注意的是一般的`ROC`类适用于二元分类，而`ROCMultiClass`适用于分类器，例如我们在此构建的模型。
`MultiLayerNetwork`内置了一系列方便的方法来帮助我们执行评估。您可以给`evaluate()`传递一些测试/验证数据的数据集迭代器。

```
System.out.println("Accuracy: " + eval.accuracy());
System.out.println("Precision: " + eval.precision());
System.out.println("Recall: " + eval.recall());

//评估操作者曲线及计算曲线下面积
ROCMultiClass roc = network.evaluateROCMultiClass(emnistTest);
System.out.println("AverageAUC: " + roc.calculateAverageAUC());

int classIndex = 0;
System.out.println("AUC: " + roc.calculateAUC(classIndex));

System.out.println(eval.stats());
System.out.println(roc.stats());
```

### 下一步

现在你已经学到了如何搭建及训练你的第一个神经网络模型，你可以前往Deeplearning4j官网查看一下等待你的的其他[教程](https://deeplearning4j.org/tutorials)。