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
隐藏层中的每个节点（上图中的圆圈）表示MNIST数据集中一个手写数字的一项特征。例如，假设现在处理的数字是6，那么一个节点可能表示圆形的边缘，另一个节点可能表示曲线的交叉点，等等。模型的系数按照重要性大小为这些特征赋予权重，随后在每个隐藏层中重新相加，帮助预测当前的手写数字是否确实为6。节点的层数更多，网络就能处理更复杂的因素，捕捉更多细节，进而做出更准确的预测。
![layers](https://deeplearning4j.org/img/onelayer_labeled.png)

之所以将中间的层称为“隐藏”层，是因为人们可以看到数据输入神经网络、判定结果输出，但网络内部的数据处理方式和原理并非一目了然。神经网络模型的参数其实就是包含许多数字、计算机可以读取的长向量。

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

| 指标 | 说明 |
| ---- | ---- |
| Accuracy | **准确率**：模型准确识别出的MNIST图像数量占总数的百分比。 |
| Precision | **精确率**：真正例的数量除以真正例与假正例数之和。 |
| Recall | **召回率**：真正例的数量除以真正例与假负例数之和。 |
| F1 Score | **F1值**：精确率和召回率的加权平均值。 |

**精确率**、**召回率**和**F1值**衡量的是模型的**相关性**。举例来说，“癌症不会复发”这样的预测结果（即假负例/假阴性）就有风险，因为病人会不再寻求进一步治疗。所以，比较明智的做法是选择一种可以避免假负例的模型（即精确率、召回率和F1值较高），尽管总体上的**准确率**可能会相对较低一些。

### 下一步

现在你已经学到了如何搭建及训练你的第一个神经网络模型，你可以前往Deeplearning4j官网查看一下等待你的的其他[教程](https://deeplearning4j.org/tutorials)。