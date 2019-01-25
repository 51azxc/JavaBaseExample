package com.example.dl4j.tutorial;

import java.io.IOException;

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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class DataIteratorExample {
	public static void main(String[] args) throws IOException {
		//图片高度及宽度
		int numRows = 28;
		int numColumns = 28;
		//最终输出分类数
		int outputNum = 10;
		//每个批次载入的数据量
		int batchSize = 128;
		//随机权重初始值
		int rngSeed = 123;
		//运行批次
		int numEpochs = 15;
		
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
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(1));
		
		DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
		DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);
		
		for (int i = 0; i <= numEpochs; i++) {
			model.fit(mnistTrain);
		}
		Evaluation eval = model.evaluate(mnistTest);
		System.out.println("Accuracy: " + eval.accuracy());
		System.out.println("Precision: " + eval.precision());
		System.out.println("Recall: " + eval.recall());
		System.out.println(eval.confusionToString());
	}
}
