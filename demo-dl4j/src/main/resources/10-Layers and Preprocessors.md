### 背景介绍

在之前的教程中我们学习了一些不同的神经网络模型，例如前馈神经网络，卷积神经网络及循环神经网络。这些网络的类型都是由其中的隐藏层的类型来决定的。比如前馈神经网络都是由密集层组成的，而循环神经网络中的隐藏层则是LSTM。在本教程中，我们将使用不同类型的神经元层来搭建一个神经网络模型。另外，我们将学习如何预处理我们的数据以便让神经网络模型的训练更加有效率。我们将搭建一个复杂的卷积神经网络模型来识别MNIST的手写数字。

### 引入类
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
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;
```

### 卷积神经网络示例

在引入了所需的类之后，我们将开始配置一个`MultiLayerNetwork`。这个网络模型包含了2个卷积层，2个最大池化层，一个密集层及一个输出层。用DL4J的功能来做是比较简单的。

首先MNIST每张图片的大小都是28X28的，这意味着图片的长宽各包含了28个像素，因此输入的数据是28X28的矩阵。在第一个卷积层中，我们使用5X5的卷积内核以长宽各1步的方式扫描整张图片，提取其单位面积的特征量，这样就得到了20个24x24的矩阵数据。然后进入第二层即最大池化层，它使用2x2的内核以长宽各2步的方式收集特征量，由于步长是2，因此会丢弃一半的特征量，最终得到的是20个12x12的矩阵。第二个卷积层的功能与第一个卷积层一致，通过这一层后，神经网络获得了50个8x8的矩阵。再经过第二个最大池化层缩小单位面积后，最终获得了50个4*4的矩阵。然后再经过一层密集层，将这些数据矢量化，因此密集层需要800个神经元。经过了这一层的处理之后，最终流入到输出层的并输出的就是10个分类，刚好对应着0-9的分布概率。
```
int outputNum = 10;
int batchSize = 64;
int seed = 123;

MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    .seed(seed)
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
        .nIn(800)
        .nOut(500).build())
    .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nIn(500)
        .nOut(outputNum)
        .activation(Activation.SOFTMAX)
        .build())
    .setInputType(InputType.convolutionalFlat(28,28,1)) 
	.backprop(true).pretrain(false).build();

MultiLayerNetwork model = new MultiLayerNetwork(conf);
```

在开始训练模型之前，我们需要将数据集转换成模型可识别的数据迭代器，同样也可以对数据进行预处理。比如这里将对数据进行一个缩放操作。由于这里使用的原始数据是灰度范围在0-255的图像，而这些图片都是单色道的黑白图像，也就是说可以用0来表示白色，1来表示黑色。这样将0-255的范围转换到0-1的范围对训练操作是十分有帮助的。我们将使用`ImagePreProcessingScaler`来对MNIST数据迭代器进行数据预处理。一旦完成，就可以开始训练数据了。
```
int rngSeed = 12345;
DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);

DataNormalization scaler = new ImagePreProcessingScaler(0,1);
scaler.fit(mnistTrain);
mnistTrain.setPreProcessor(scaler);
mnistTest.setPreProcessor(scaler);
```
我们将训练批次设置为5
```
for (int i = 1; i <= 5; i++) {
	model.fit(mnistTrain);
	System.out.println("Epoch " + i + " complete");
}
```
最后我们使用测试数据来对训练好的模型进行评估。
```
Evaluation eval = model.evaluate(mnistTest);
System.out.println(eval.stats());
```
这里来看我们仅训练了5个批次这个模型的表现还挺不错的。