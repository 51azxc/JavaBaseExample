package com.example.dl4j.tutorial;

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

public class CnnTest1 {

	public static void main(String[] args) throws Exception {
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
		
		int rngSeed = 12345;
		//直接下载mnist数据并分成训练/测试数据
		DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
		DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);
		
		DataNormalization scaler = new ImagePreProcessingScaler(0,1);
		scaler.fit(mnistTrain);
		mnistTrain.setPreProcessor(scaler);
		mnistTest.setPreProcessor(scaler);
		
		for (int i = 1; i < 5; i++) {
			model.fit(mnistTrain);
			System.out.println("Epoch " + i + " complete");
		}
		
		Evaluation eval = model.evaluate(mnistTest);
		System.out.println(eval.stats());
		
	}

}
