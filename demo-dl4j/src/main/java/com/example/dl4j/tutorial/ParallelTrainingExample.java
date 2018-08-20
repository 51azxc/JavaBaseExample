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
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.parallelism.ParallelWrapper;

public class ParallelTrainingExample {

	public static void main(String[] args) throws Exception {
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
		
		ParallelWrapper wrapper = new ParallelWrapper.Builder<MultiLayerNetwork>(model)
				.prefetchBuffer(24)
	            .workers(2)
	            .averagingFrequency(3)
	            .reportScoreAfterAveraging(true)
				.build();
		
		DataSetIterator mnistTrain = new MnistDataSetIterator(128, true, 12345);
		DataSetIterator mnistTest = new MnistDataSetIterator(128, false, 12345);
		
		wrapper.fit(mnistTrain);
		Evaluation eval = model.evaluate(mnistTest);
		System.out.println(eval.stats());
		
	}

}
