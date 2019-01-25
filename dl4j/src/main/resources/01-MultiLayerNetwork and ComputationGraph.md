### DL4J 神经网络架构
----

DL4j提供了以下两个配置神经网络的类:
1. `MultiLayerNetwork`: 由一个输入层，一个输出层，中间多个神经元层组成。
2. `ComputationGraph`: 用于构建比`MultiLayerNetwork`更加复杂的神经网络架构。它支持多个输入层，多个输出层，以及在其中可以通过指向性非循环图层来连接。

----

### 神经网络配置类

无论你想创建`MultiLayerNetwork`还是`ComputationGraph`,你需要先用`NeuralNetConfiguration.Builder`来创建一个神经网络配置类。正如其名，它是使用了建造者模式来构建一个神经网络配置。如果需要创建`MultiLayerNetwork`,就需要生成一个`MultiLayerConfiguraion`类，同样需要创建`ComputationGraph`则需要`ComputationGraphConfiguration`。

整个配置的创建顺序类似这样: [设置超参数] -> [搭建层] -> [配置预训练及反向传播] -> [建立配置类]。

需要引入的类:
```java
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
```

### 创建MultiLayerConfiguration:
```java
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    //将一组随机生成的权重确定为初始权重
	.seed(123)
	//随机梯度下降（Stochastic Gradient Descent，SGD）是一种用于优化代价函数的常见方法
	.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	//权重更新器
	.updater(new Nesterovs(0.9))
	//指定网络中层的数量；它会将您的配置复制n次，建立分层的网络结构
	.list()
	.layer(0, new DenseLayer.Builder()
			.nIn(784)
			.nOut(100)
			.weightInit(WeightInit.XAVIER)
	      	.activation(Activation.RELU)
	        .build())
	 .layer(1, new OutputLayer.Builder()
	        .nIn(100)
			.nOut(10)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.RELU)
			.build())
	  .pretrain(false).backprop(true)
	  .build();
```

### 概要说明

#### 设置超参数

| 方法 | 说明 |
| ---- | ---- |
| seed | 该参数将一组随机生成的权重确定为初始权重。如果一个示例运行很多次，而每次开始时都生成一组新的随机权重，那么神经网络的表现（准确率和F1值）有可能会出现很大的差异，因为不同的初始权重可能会将算法导向误差曲面上不同的局部极小值。在其他条件不变的情况下，保持相同的随机权重可以使调整其他超参数所产生的效果表现得更加清晰。|
| optimizationAlgo | 随机梯度下降（Stochastic Gradient Descent，SGD）是一种用于优化代价函数的常见方法。|
| updater | 动量（`momentum`）是另一项决定优化算法向最优值收敛的速度的因素。动量影响权重调整的方向，所以在代码中，我们将其视为一种权重的更新器（`updater`）。|

#### 搭建层

这里我们会调用`list()`方法来获取`ListBuilder`。它提供了一些必要的API用来添加神经网络层。例如`layer(arg1, arg2)`方法：

* 第一个参数表明神经层添加的位置。
* 第二个参数则是添加到神经网络中的神经层类型。

创建一个神经层我们需要调用以下的方法来配置建造模式:

| 方法 | 说明 |
| ---- | ---- |
| nIn | 从上一层传递下来的神经元数作为本层的输入数据大小。(对于第一个神经元层来说，它意为从输入层获取的输入数据大小) |
| nOut | 用于传递给下一个神经元层的输出数据。(对于输出层来说则是标签(labels)数) |
| weightInit | 指定用于神经元层参数的权重初始化方式。可以通过`WeightInit.values().foreach { println }`来查看各种不同的权重初始化方式 |
| activation | 在神经元层之间使用的激活函数。可以通过`Activation.values().foreach { println }`来查看各种不同的激活函数 |

#### 预训练及反向传播配置

| 方法 | 说明 |
| ---- | ---- |
| pretrain | 为FALSE的情况下则会从头开始训练 |
| backprop | 决定是否反向传播 |


----

#### 构建图层

最后，调用`build()`方法即可为我们创建好一个配置类。

#### 检查`MultiLayerConfiguration`

你可以将你的神经网络配置类输出成String,JSON或者YAML来查看一番。例如可以通过`toJson()`方法来输出JSON格式的配置类:
```java
System.out.println(conf.toJson());
```

#### 创建MultiLayerNetwork

最终我们通过创建好的配置类来创建一个`MultiLayerNetwork`:
```java
MultiLayerNetwork net = new MultiLayerNetwork(conf);
```

----

### 创建ComputationGraphConfiguration
```java
ComputationGraphConfiguration cgConf = new NeuralNetConfiguration.Builder()
	.seed(123)
	.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	.graphBuilder()
	.addInputs("input")
	.addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input")
	.addLayer("out1", new OutputLayer.Builder()
						.lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
						.nIn(4).nOut(3).build(), "L1")
	.addLayer("out2", new OutputLayer.Builder()
			.lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
			.nIn(4).nOut(2).build(), "L1")
	.setOutputs("out1", "out2")
	.pretrain(false).backprop(true)
	.build();
```

### 概要说明

与创建`MultiLayerConfiguration`唯一不同的是，我们使用了`graphBuilder()`方法来代替`list()`方法构建`ComputationGraphConfiguration`。以下是`graphBuilder()`的各个方法:
| 方法 | 说明 |
| ---- | ---- |
| addInputs | 接收一个字符串列表，告诉神经网络哪些层作为输入层 |
| addLayer | 第一个参数为层名，然后是图层对象，最后的参数则是一个数组列表用来定义上一组中哪些层作为此层的输入层 |
| setOutputs | 一个字符串数组用来定义哪些层是输出层 |

输出层可以使用`lossFunction`方法来定义需要使用哪个计算损失率函数。可以使用`LossFunctions.LossFunction.values().foreach { println }`来查看哪些损失函数可用。

你可以将你的神经网络配置类输出成String,JSON或者YAML来查看一番。例如可以通过`toJson()`方法来输出JSON格式的配置类:
```java
System.out.println(cgConf.toJson());
```

#### ComputationGraph

最终我们通过创建好的配置类来创建一个`ComputationGraph`:
```java
ComputationGraph graph = new ComputationGraph(cgConf);
```

----

### 更多关于MultiLayerConfiguration的示例

1.正则化
你可以在超参数配置环节中指定正则化函数。主要有l1，l2算法
```
new NeuralNetConfiguration.Builder().l2(1e-4);
```

2.丢弃连接数
```
new NeuralNetConfiguration.Builder().list().layer(0, new DenseLayer.Builder().dropOut(0.8).build());
```

3.指定偏移量初始值
```
new NeuralNetConfiguration.Builder().list().layer(0, new DenseLayer.Builder().biasInit(0).build());
```

----

### 更多关于ComputationGraphConfiguration的示例

1.具有跳过连接数的循环神经网络
```
ComputationGraphConfiguration cgConf1 = new NeuralNetConfiguration.Builder()
	.graphBuilder()
	.addInputs("input")
    .addLayer("L1", new LSTM.Builder().nIn(5).nOut(5).build(), "input")
    .addLayer("L2",new RnnOutputLayer.Builder().nIn(5+5).nOut(5).build(), "input", "L1")
    .setOutputs("L2")
	.build();
```

2.多输入值及顶点合并
```
ComputationGraphConfiguration cgConf2 = new NeuralNetConfiguration.Builder()
	.graphBuilder()
	.addInputs("input1", "input2")
	.addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input1")
    .addLayer("L2", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input2")
    .addVertex("merge", new MergeVertex(), "L1", "L2")
    .addLayer("out", new OutputLayer.Builder().nIn(4+4).nOut(3).build(), "merge")
    .setOutputs("out")
	.build();
```

3.多任务学习
```
ComputationGraphConfiguration cgConf3 = new NeuralNetConfiguration.Builder()
	.graphBuilder()
	.addInputs("input")
    .addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input")
    .addLayer("out1", new OutputLayer.Builder()
            .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
            .nIn(4).nOut(3).build(), "L1")
    .addLayer("out2", new OutputLayer.Builder()
            .lossFunction(LossFunctions.LossFunction.MSE)
            .nIn(4).nOut(2).build(), "L1")
    .setOutputs("out1","out2")
	.build();
```
