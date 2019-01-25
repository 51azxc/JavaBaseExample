### 背景介绍

在我们前边的教程中，我们学习了一个比较简单的神经网络模型 - 逻辑回归模型。虽然你可以使用这样的简单模型解决许多任务，但大多数需要解决的问题都要复杂的网络配置。典型的深度模型在输入层与输出层之间有许多隐藏层组成。在这个教程中，我们将了解其中一种：前馈神经网络。

### 前馈神经网络

前馈网络是指网络中各个神经元层之间没有循环连接。数据通过输入层进入到网络中，经过几个中间层后，最终流入到了输出层。典型的前馈网络如下所示：

![feed-forward](https://upload.wikimedia.org/wikipedia/en/5/54/Feed_forward_neural_net.gif)

在这里你可以看到一个之前没见过的名词：隐藏层。隐藏层是指输入层与输出层之间所有的神经元层。因为不与我们直接打交道，因此里边的操作对我们不可见，所以称之为隐藏层。神经网络中可以有多个隐藏层。

神经网络中的每一层都可以使用激活函数，正如上一个教程中我们给输出层添加了softmax激活函数一样。它们将负责我们神经网络中输出到下一层的节点是否允许激活点。当然DL4J提供了不同的激活函数，如sigmoid和relu等。

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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
```
接下来我们开始创建一个前馈神经网络的配置类
```
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    .seed(12345)
    .weightInit(WeightInit.XAVIER)
    .updater(new AdaGrad(0.05))
    .activation(Activation.RELU)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .l2(0.0001)
    .list()
	.layer(0, new DenseLayer.Builder()
			.nIn(784)
			.nOut(250)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.RELU)
			.build())
	.layer(1, new OutputLayer.Builder()
			.nIn(250)
			.nOut(10)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.SOFTMAX)
			.lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
			.build())
    .pretrain(false).backprop(true)
    .build();
```
你可以看到上边的代码创建了拥有一个隐藏层的前馈神经网络模型配置类。在隐藏层与输出层之间我们使用了当今使用率最高的激活函数：RELU。激活函数还给我们的网络模型带来了非线性因素，这样我们的模型可以从数据中提取到更复杂的特征。隐藏层可以从输入层中的数据学习不同的特征，并且还可以将这些特征加以分析然后发送给输出层以获取对应的输出值。你也可以给网络模型配置更多的隐藏层:
```
//确定每一层的输入值需要等于上一层的输出值
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    .seed(12345)
    .weightInit(WeightInit.XAVIER)
    .updater(new AdaGrad(0.05))
    .activation(Activation.RELU)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .l2(0.0001)
    .list()
    .layer(0, new DenseLayer.Builder()
			.nIn(784)
			.nOut(250)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.RELU)
			.build())
    .layer(1, new DenseLayer.Builder()
			.nIn(250)
			.nOut(100)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.RELU)
			.build())
    .layer(2, new DenseLayer.Builder()
			.nIn(100)
			.nOut(50)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.RELU)
			.build())
    .layer(3, new OutputLayer.Builder()
			.nIn(50)
			.nOut(10)
			.weightInit(WeightInit.XAVIER)
			.activation(Activation.SOFTMAX)
			.lossFunction(LossFunction.NEGATIVELOGLIKELIHOOD)
			.build())
    .pretrain(false).backprop(true)
    .build();
```