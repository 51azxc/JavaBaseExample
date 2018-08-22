package com.example.dl4j.tutorial;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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


public class HyperparametersExample {

	public static void main(String[] args) throws Exception {
		
		//配置超参数的范围。学习速率在0.001-0.1之间，网络层数在16与256之间
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
		
		//自定义一个类调用Mnist数据迭代器作为数据源
        Class<? extends DataSource> dataSource = MnistDataSource.class;
        Properties properties = new Properties();
        //指定批次数
		properties.setProperty("batchSize", "64");

		//通过判定分类的准确率来决定分数
		ScoreFunction scoreFunction = new EvaluationScoreFunction(Metric.ACCURACY);
		
		//配置停止搜寻条件: 运行超过15分钟或者查找的候选者达到10个
		TerminationCondition[] terminationConditions = {
	            new MaxTimeCondition(15, TimeUnit.MINUTES),
	            new MaxCandidatesCondition(10)};
		
		//存储生成的模型
		Path dirFile = Paths.get(System.getProperty("java.io.tmpdir"), "arbiterExample");
		if (Files.exists(dirFile, LinkOption.NOFOLLOW_LINKS)) {
			Files.delete(dirFile);
		} else {
			Files.createDirectory(dirFile);
		}
		ResultSaver modelSaver = new FileModelSaver(dirFile.toString());
		
		//生成优化配置
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
	}

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

}
