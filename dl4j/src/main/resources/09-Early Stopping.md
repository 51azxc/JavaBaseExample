<<<<<<< HEAD:dl4j/src/main/resources/09-Early Stopping.md
### 背景介绍

训练神经网络模型中需要避免出现过拟合的问题。过拟合主要是指神经网络在训练过程中对一些不怎么符合常理的训练数据过度的学习从而导致不能很好的识别未训练的数据。神经网络发生过拟合的情况与训练批次这一超参数有关，也与训练/测试数据的比例有关。如果训练批次过多，那参与训练的神经网络模型极有可能发生过拟合现象。另一方面如果训练批次过少，那整个神经网络模型可能不能从训练数据中学到什么（欠拟合）。

提前停止(Early stopping)是一种手动设置训练批次以处理过拟合及欠拟合的机制。它背后的想法是直观的。首先数据分为训练数据与测试数据，每一次模型训练完毕后，都会与测试数据进行评估，如果本次的成果优与上一次，那就保存这一次的。最后将最佳的模型作为最终输出成果。

在本教材中，我们将展示如何使用DL4J的提前停止功能。我们将使用前馈神经网络模型来识别MNIST数据集中的手写数字。

### 引入类
```
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
```

在引入了所需的类之后，我们将使用之前配置好的神经网络模型及数据集迭代器:
```
int numRows = 28;
int numColumns = 28;
int outputNum = 10;
int batchSize = 128;
int rngSeed = 123;

DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);

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
```

如果不使用提前停止机制的话，这里将指定训练批次通过for循环来训练模型。如果使用的话，在这里的话我们需要配置好一个`EarlyStoppingConfiguration`类，然后指定训练的最多批次为10次，或者是训练时间为5分钟。每次训练完一个批次都会调用mnistTest数据迭代器来评估模型，每一个模型都会存到指定的目录中(DL4JEarlyStoppingExample)。
配置好了`EarlyStoppingConfiguration`之后，我们需要配置对应的训练数据集`EarlyStoppingTrainer`,然后通过它的`fit`方法开始训练。
```
Path dirFile = Paths.get(System.getProperty("java.io.tmpdir"), "DL4JEarlyStoppingExample");
if (Files.notExists(dirFile, LinkOption.NOFOLLOW_LINKS)) {
	Files.createDirectory(dirFile);
}

LocalFileModelSaver saver = new LocalFileModelSaver(dirFile.toString());
EarlyStoppingConfiguration<MultiLayerNetwork> esConf = new EarlyStoppingConfiguration.Builder<MultiLayerNetwork>()
		.epochTerminationConditions(new MaxEpochsTerminationCondition(10))
		.iterationTerminationConditions(new MaxTimeIterationTerminationCondition(5, TimeUnit.MINUTES))
		.scoreCalculator(new DataSetLossCalculator(mnistTest, true))
		.evaluateEveryNEpochs(1)
		.modelSaver(saver)
		.build();

EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, conf, mnistTrain);
EarlyStoppingResult<MultiLayerNetwork> result = trainer.fit();
```

最终我们可以输出效果最好的那个模型:
```
System.out.println("Termination reason: " + result.getTerminationReason());
System.out.println("Termination details: " + result.getTerminationDetails());
System.out.println("Total epochs: " + result.getTotalEpochs());
System.out.println("Best epoch number: " + result.getBestModelEpoch());
System.out.println("Score at best epoch: " + result.getBestModelScore());
```
=======
### 背景介绍

训练神经网络模型中需要避免出现过拟合的问题。过拟合主要是指神经网络在训练过程中对一些不怎么符合常理的训练数据过度的学习从而导致不能很好的识别未训练的数据。神经网络发生过拟合的情况与训练批次这一超参数有关，也与训练/测试数据的比例有关。如果训练批次过多，那参与训练的神经网络模型极有可能发生过拟合现象。另一方面如果训练批次过少，那整个神经网络模型可能不能从训练数据中学到什么（欠拟合）。

早停法(Early stopping)是一种手动设置训练批次以处理过拟合及欠拟合的机制。它背后的想法是直观的。首先数据分为训练数据与测试数据，每一次模型训练完毕后，都会与测试数据进行评估，如果本次的成果优与上一次，那就保存这一次的。最后将最佳的模型作为最终输出成果。

在本教材中，我们将展示如何使用DL4J的提前停止功能。我们将使用前馈神经网络模型来识别MNIST数据集中的手写数字。

### 引入类
```
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
```

在引入了所需的类之后，我们将使用之前配置好的神经网络模型及数据集迭代器:
```
int numRows = 28;
int numColumns = 28;
int outputNum = 10;
int batchSize = 128;
int rngSeed = 123;

DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);

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
```

如果不使用早停法的话，这里将指定训练批次通过for循环来训练模型。如果使用的话，在这里的话我们需要配置好一个`EarlyStoppingConfiguration`类，然后指定训练的最多批次为10次，或者是训练时间为5分钟。每次训练完一个批次都会调用mnistTest数据迭代器来评估模型，每一个模型都会存到指定的目录中(DL4JEarlyStoppingExample)。
配置好了`EarlyStoppingConfiguration`之后，我们需要配置对应的训练数据集`EarlyStoppingTrainer`,然后通过它的`fit`方法开始训练。
```
Path dirFile = Paths.get(System.getProperty("java.io.tmpdir"), "DL4JEarlyStoppingExample");
if (Files.notExists(dirFile, LinkOption.NOFOLLOW_LINKS)) {
	Files.createDirectory(dirFile);
}

LocalFileModelSaver saver = new LocalFileModelSaver(dirFile.toString());
EarlyStoppingConfiguration<MultiLayerNetwork> esConf = new EarlyStoppingConfiguration.Builder<MultiLayerNetwork>()
		.epochTerminationConditions(new MaxEpochsTerminationCondition(10))
		.iterationTerminationConditions(new MaxTimeIterationTerminationCondition(5, TimeUnit.MINUTES))
		.scoreCalculator(new DataSetLossCalculator(mnistTest, true))
		.evaluateEveryNEpochs(1)
		.modelSaver(saver)
		.build();

EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, conf, mnistTrain);
EarlyStoppingResult<MultiLayerNetwork> result = trainer.fit();
```

最终我们可以输出效果最好的那个模型:
```
System.out.println("Termination reason: " + result.getTerminationReason());
System.out.println("Termination details: " + result.getTerminationDetails());
System.out.println("Total epochs: " + result.getTotalEpochs());
System.out.println("Best epoch number: " + result.getBestModelEpoch());
System.out.println("Score at best epoch: " + result.getBestModelScore());
```
>>>>>>> 8e090b650a6a841137a3e1a28f4567235c236072:demo-dl4j/src/main/resources/09-Early Stopping.md
