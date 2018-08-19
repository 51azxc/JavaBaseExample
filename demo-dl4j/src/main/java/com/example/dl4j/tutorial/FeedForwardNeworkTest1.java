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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class FeedForwardNeworkTest1 {

	public static void main(String[] args) throws IOException {
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
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(10));
		
		DataSetIterator mnistTrain = new MnistDataSetIterator(128, true, 12345);
		DataSetIterator mnistTest = new MnistDataSetIterator(128, false, 12345);
		
		for (int i = 0; i <= 15; i++) {
			model.fit(mnistTrain);
		}
		
		//创建一个包含了10个预测分类的评估模型
		Evaluation eval = new Evaluation(10);
		while(mnistTest.hasNext()) {
			DataSet next = mnistTest.next();
			//获取网络生成的预测值
			INDArray output = model.output(next.getFeatures());
			//对比预测值与真正分类之间的准确度
			eval.eval(next.getLabels(), output);
		}
		System.out.println(eval.stats());
	}

}
