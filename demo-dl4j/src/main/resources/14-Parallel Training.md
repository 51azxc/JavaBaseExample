### 背景介绍

训练神经网络模型是一件十分昂贵的事情，因为他对处理器计算能力要求很高。如果你的计算机有GPU的硬件设施的话，可以选择使用多个GPU并行训练，这样可以加快训练速度。在本教程中，我们使用多个GPU来并行的训练前馈神经网络。

### 引入类
首先需要引入全新的依赖:
```
<dependency>
  <groupId>org.deeplearning4j</groupId>
  <artifactId>deeplearning4j-parallel-wrapper_2.11</artifactId>
  <version>1.0.0-beta2</version>
</dependency>
```
然后再是引入必需的类:
```
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.parallelism.ParallelWrapper;
```

首先还是得先引入需要训练的数据：
```
DataSetIterator mnistTrain = new MnistDataSetIterator(128, true, 12345);
DataSetIterator mnistTest = new MnistDataSetIterator(128, false, 12345);
```
接下来，就是配置一个卷积神经网络了:
```
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	    .seed(123)
	    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	    .list()
	    .layer(0, new ConvolutionLayer.Builder(5, 5)
	        .nIn(1)
	        .stride(1, 1)
	        .nOut(20)
	        .activation(Activation.IDENTITY)
	        .build())
	    .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
	        .kernelSize(2,2)
	        .stride(2,2)
	        .build())
	     .layer(2, new ConvolutionLayer.Builder(5, 5)
	        .stride(1, 1)
	        .nOut(50)
	        .activation(Activation.IDENTITY)
	        .build())
	    .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
	        .kernelSize(2,2)
	        .stride(2,2)
	        .build())
	    .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
	        .nOut(500).build())
	    .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	        .nIn(500)
	        .nOut(10)
	        .activation(Activation.SOFTMAX)
	        .build())
	    .setInputType(InputType.convolutionalFlat(28,28,1)) 
		.backprop(true).pretrain(false).build();

MultiLayerNetwork model = new MultiLayerNetwork(conf);
model.init();
```

接下来就是使用`ParallelWrapper`来配置并行训练的相关操作。它会让参与训练的GPU负载均衡。

指定参与训练的"工人们"(workers，即GPU硬件，这里指定2，最低要求)，然后指定平均迭代次数(这里是3),这样模型会被均衡的分配给每个参与训练的硬件设施，他们会不断的训练模型，最终使得模型得到充分的训练。

```
ParallelWrapper wrapper = new ParallelWrapper.Builder<MultiLayerNetwork>(model)
	.prefetchBuffer(24)
    .workers(2)
    .averagingFrequency(3)
    .reportScoreAfterAveraging(true)
	.build();
```
最后直接调用`fit`方法就可以并行训练了。
```
wrapper.fit(mnistTrain);
```