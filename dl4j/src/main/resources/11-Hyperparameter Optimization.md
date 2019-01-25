### 背景介绍

神经网络的超参数是在训练之前设置的参数。它们包括学习速率，批量大小，训练批次，正则化，初始化权重值方法，隐藏层数量，神经元节点等等。与神经网络中的节点权重值和偏移量不同的是，它们不能使用数据直接预计。设置超参数的优劣对神经网络的性能影响十分大，因此需要花费许多的时间来调试超参数。DL4J提供了一系列的工具来做这项工作，Arbiter就是其中之一，它是为调整神经网络模型而生的。在这个教程里，我们将展示一个使用Arbiter来微调神经网络模型的学习速率及层数的例子。我们还是使用MNIST数据集来训练我们的神经网络。

### 引入类
```
import org.deeplearning4j.arbiter.MultiLayerSpace;
import org.deeplearning4j.arbiter.conf.updater.SgdSpace;
import org.deeplearning4j.arbiter.data.MnistDataProvider;
import org.deeplearning4j.arbiter.layers.DenseLayerSpace;
import org.deeplearning4j.arbiter.layers.OutputLayerSpace;
import org.deeplearning4j.arbiter.optimize.api.CandidateGenerator;
import org.deeplearning4j.arbiter.optimize.api.OptimizationResult;
import org.deeplearning4j.arbiter.optimize.api.ParameterSpace;
import org.deeplearning4j.arbiter.optimize.api.data.DataSource;
import org.deeplearning4j.arbiter.optimize.api.saving.ResultReference;
import org.deeplearning4j.arbiter.optimize.api.saving.ResultSaver;
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction;
import org.deeplearning4j.arbiter.optimize.api.termination.MaxCandidatesCondition;
import org.deeplearning4j.arbiter.optimize.api.termination.MaxTimeCondition;
import org.deeplearning4j.arbiter.optimize.api.termination.TerminationCondition;
import org.deeplearning4j.arbiter.optimize.config.OptimizationConfiguration;
import org.deeplearning4j.arbiter.optimize.generator.RandomSearchGenerator;
import org.deeplearning4j.arbiter.optimize.parameter.continuous.ContinuousParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace;
import org.deeplearning4j.arbiter.optimize.runner.IOptimizationRunner;
import org.deeplearning4j.arbiter.optimize.runner.LocalOptimizationRunner;
import org.deeplearning4j.arbiter.saver.local.FileModelSaver;
import org.deeplearning4j.arbiter.scoring.impl.EvaluationScoreFunction;
import org.deeplearning4j.arbiter.task.MultiLayerNetworkTaskCreator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation.Metric;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
```
需要在`pom.xml`中加入Aribter依赖:
```
 <dependency>
  <groupId>org.deeplearning4j</groupId>
  <artifactId>arbiter-core</artifactId>
  <version>1.0.0-beta2</version>
</dependency>

<dependency>
  <groupId>org.deeplearning4j</groupId>
  <artifactId>arbiter-deeplearning4j</artifactId>
  <version>1.0.0-beta2</version>
</dependency>
```

我们的目标是对模型的学习速率及层数进行微调。首先需要为它们设定范围。我们给学习速率设定的区间是0.001-0.1，而层数则是16-255。

接下来，我们将会配置一个跟下边的`MultiLayerNetwork`十分相似的类`MultiLayerSpace`。在这里跟使用`MultiLayerNetwork`直接设置超参数不同的是，我们需要需要使用`ParameterSpaces`来设置学习速率和隐藏层的节点数量。

最后，我们使用`CandidateGenerator`类来配置将要生成的学习速率及图层数的候选值。在本教程中，我们使用的是随机搜索的方式，这样两个候选值将在指定的范围内统一生成。
```
ParameterSpace<Double> learningRateHyperparam  = new ContinuousParameterSpace(0.0001, 0.1);
ParameterSpace<Integer> layerSizeHyperparam  = new IntegerParameterSpace(16,256);

MultiLayerSpace hyperparameterSpace = new MultiLayerSpace.Builder()
	.weightInit(WeightInit.XAVIER)
	.l2(0.0001)  
	//用于查找出最准确的学习速率
	.updater(new SgdSpace(learningRateHyperparam))
	.addLayer(new DenseLayerSpace.Builder().activation(Activation.LEAKYRELU)
			//输入参数固定位28*28=784，为mnist的单张图片的总像素
			//输出值位需要配置的超参数，网络层数
			.nIn(784).nOut(layerSizeHyperparam).build())
	.addLayer(new OutputLayerSpace.Builder().activation(Activation.SOFTMAX)
			.nOut(10).lossFunction(LossFunction.MCXENT).build())
	.build();

//通过随机查找方式来确定潜在候选配置
CandidateGenerator candidateGenerator = new RandomSearchGenerator(hyperparameterSpace, null);
```

为了能够获得数据，我们接下来需要新建一个数据源类:
```
public static class MnistDataSource implements DataSource {
    // 一次取出的数据数
    private int batchSize;
    private int rngSeed = 12345;

    public MnistDataSource() { }

    @Override
    public void configure(Properties properties) {
        this.batchSize = Integer.parseInt(properties.getProperty("batchSize", "64"));
    }

    @Override
    public Object trainData() {
        DataSetIterator trainIter = null;
        try {
            //调用MnistDataSetIterator获取训练数据
            trainIter = new MnistDataSetIterator(batchSize, true, rngSeed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trainIter;
    }

    @Override
    public Object testData() {
        DataSetIterator testIter = null;
        try {
            //调用MnistDataSetIterator获取测试数据
            testIter = new MnistDataSetIterator(batchSize, false, rngSeed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testIter;
    }

    @Override
    public Class<?> getDataType() {
        //指定数据类型
        return DataSetIterator.class;
    }
}
```
然后指定指定数据源每次抓取的数据量为64:
```
//自定义一个类调用Mnist数据迭代器作为数据源
Class<? extends DataSource> dataSource = MnistDataSource.class;
Properties properties = new Properties();
//指定批次数
properties.setProperty("batchSize", "64");
```
接下来我们使用`TestSetAccuracyScoreFunction`来评估各种不同配置下的超参数：
```
//通过判定分类的准确率来决定分数
ScoreFunction scoreFunction = new EvaluationScoreFunction(Metric.ACCURACY);
```
最后需要设置的是终止条件。条件设置了两个：运行达到了15分钟或者得到了10个不同的超参数配置：
```
TerminationCondition[] terminationConditions = {
        new MaxTimeCondition(15, TimeUnit.MINUTES),
        new MaxCandidatesCondition(10)};
```
当然不要忘了我们需要将最好的模型给保存下来:
```
Path dirFile = Paths.get(System.getProperty("java.io.tmpdir"), "arbiterExample");
if (Files.exists(dirFile, LinkOption.NOFOLLOW_LINKS)) {
	Files.delete(dirFile);
} else {
	Files.createDirectory(dirFile);
}
ResultSaver modelSaver = new FileModelSaver(dirFile.toString());
```
先行条件都准备好了，接下来就是配置执行查找最优超参数的`IOptimizaitonRunner `了。
```
OptimizationConfiguration configuration = new OptimizationConfiguration.Builder()
    .candidateGenerator(candidateGenerator)
    .dataSource(dataSource, properties)
    .modelSaver(modelSaver)
    .scoreFunction(scoreFunction)
    .terminationConditions(terminationConditions)
    .build();

IOptimizationRunner runner = new LocalOptimizationRunner(configuration, new MultiLayerNetworkTaskCreator());
//开始优化超参数
runner.execute();
```

最终打印结果:
```
System.out.println("Best score: " + runner.bestScore() + "\n" +
			               "Index of model with best score: " + runner.bestScoreCandidateIndex() + "\n" +
			               "Number of configurations evaluated: " + runner.numCandidatesCompleted() + "\n");
		
//获取所有的结果，从中取出最好的结果
int indexOfBestResult = runner.bestScoreCandidateIndex();
List<ResultReference> allResults = runner.getResults();
 
OptimizationResult bestResult = allResults.get(indexOfBestResult).getResult();
MultiLayerNetwork bestModel = (MultiLayerNetwork)bestResult.getResultReference().getResultModel();

System.out.println("\n\nConfiguration of best model:\n");
System.out.println(bestModel.getLayerWiseConfigurations().toJson());
```