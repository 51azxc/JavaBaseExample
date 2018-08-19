package com.example.dl4j.tutorial;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

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

public class EarlyStoppingTest1 {
	public static void main(String[] args) throws Exception {
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
		
		//MultiLayerNetwork model = new MultiLayerNetwork(conf);
		
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
		
		System.out.println("Termination reason: " + result.getTerminationReason());
		System.out.println("Termination details: " + result.getTerminationDetails());
		System.out.println("Total epochs: " + result.getTotalEpochs());
		System.out.println("Best epoch number: " + result.getBestModelEpoch());
		System.out.println("Score at best epoch: " + result.getBestModelScore());
		
	}
}
