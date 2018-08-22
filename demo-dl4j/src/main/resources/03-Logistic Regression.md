### 背景介绍

通过深度学习，我们可以组成一个适配输入数据及其特征深度神经网络，而训练这个网络模型的目标则是可以让他对数据进行预测，这些预测结果与你关心的结果是否有关联。例如一场交易中是否存在合同欺骗；在一张照片中包含了哪些对象？配置神经网络有不同的方法，不过它们都构建了输入层与输出层之间的层次关系结构。

在这个教程中，我们将配置一个最简单的逻辑回归神经网络模型。

回归(Regression)是一个有助于显示自变量(输入值)与因变量(输出值)之间的条件期望。逻辑回归的因变量是分类排列而不是连续排列的 - 这意为着它只能预测一个有限数量的分类或者类别，例如你的开关是否打开，又或者它可以预测一张图片中是否包含了猫或狗，再如从0-9中识别出10个数字。

一个最简单的逻辑回归分析就是计算方程式` y = W*x + b`。`x`为自变量，`W`则为权重值，`b`是偏差值，`y`则是因变量或者预测的输出数据。这个生物学术语表明人工神经元如何松散的映射到人类大脑中的神经元。最重要的一点是其中数据如何流转并进行了结构转换。

![lr](https://i.pinimg.com/736x/61/fe/81/61fe81589ab491d1d3ba612b3bdf5b51--convolutional-neural-network-neuron-model.jpg)

我们接下来将配置一个最简单的神经网络，仅仅包含了一个输入层及一个输出层，用来展示逻辑回归如何工作。

### 配置逻辑回归层

我们先构建神经元层，然后将它们加入到网络模型配置中。
```
//建立输出层
OutputLayer outputLayer = new OutputLayer.Builder()
    //输入神经单元数
    .nIn(784)
    //输出神经单元数
    .nOut(10)
    //权重初始化算法
    .weightInit(WeightInit.XAVIER)
    //指定激活函数，softmax用作将输出层的神经元转换成概率分布数据
    .activation(Activation.SOFTMAX)
    //建立输出层
    .build();

MultiLayerConfiguration logisticRegressionConf = new NeuralNetConfiguration.Builder()
    .seed(123).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .updater(new Nesterovs(0.1, 0.9)) //以上为超参数配置
    .list()
    //输出层
    .layer(0, outputLayer)
    .pretrain(false).backprop(true)
    .build();
```

(图已裂......)在这里我们可以看到x1,x2,x3...xn为我们的输入层，而z1,z2,z3...zn为我们的输出层。在这里了解权重和偏差是如何连接的，以及如何应用softmax来给出概率分布。

### 等等，输入层呢？

你可能也看到了为啥这里没有输入层的相关代码呢。因为输入层仅仅是在网络模型中用来接收输入数据的，它并没有执行任何的计算，仅仅是将需要训练或者评估的数据转化成神经网络可读的数据传入到网络模型中。不过不必担心，稍后我们将使用数据迭代器，它将以特定模式输入到网络中，也就是一个网络的输入层了。

