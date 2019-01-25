package com.example.dl4j.tutorial;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class MultiLayerNetworkExample {

	public static void main(String[] args) {
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				//将一组随机生成的权重确定为初始权重
	            .seed(123)
	            //随机梯度下降（Stochastic Gradient Descent，SGD）是一种用于优化代价函数的常见方法
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            //权重更新器
	            .updater(new Nesterovs(0.9))
	            //指定网络中层的数量；它会将您的配置复制n次，建立分层的网络结构
	            .list()
	            .layer(0, new DenseLayer.Builder()
	            			.nIn(784)
	            			.nOut(100)
	            			.weightInit(WeightInit.XAVIER)
	            			.activation(Activation.RELU)
	            			.build())
	            .layer(1, new OutputLayer.Builder()
	            			.nIn(100)
	            			.nOut(10)
	            			.weightInit(WeightInit.XAVIER)
	            			.activation(Activation.RELU)
	            			.build())
	            .pretrain(false).backprop(true)
	            .build();
		System.out.println(conf.toJson());
		
		MultiLayerNetwork net = new MultiLayerNetwork(conf);
		System.out.println(net.getnLayers());
		
		ComputationGraphConfiguration cgConf = new NeuralNetConfiguration.Builder()
				.seed(123)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.graphBuilder()
				.addInputs("input")
				.addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input")
				.addLayer("out1", new OutputLayer.Builder()
									.lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
									.nIn(4).nOut(3).build(), "L1")
				.addLayer("out2", new OutputLayer.Builder()
						.lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
						.nIn(4).nOut(2).build(), "L1")
				.setOutputs("out1", "out2")
				.pretrain(false).backprop(true)
				.build();
		System.out.println(cgConf.toJson());
		
		ComputationGraph graph = new ComputationGraph(cgConf);
		System.out.println(graph.getNumOutputArrays());
		
		//正则化
		new NeuralNetConfiguration.Builder().l2(1e-4);
		//指定丢弃数量
		new NeuralNetConfiguration.Builder().list().layer(0, new DenseLayer.Builder().dropOut(0.8).build());
		//偏移量初始化
		new NeuralNetConfiguration.Builder().list().layer(0, new DenseLayer.Builder().biasInit(0).build());
		
		//跳过指定连接数量的循环神经网络
		ComputationGraphConfiguration cgConf1 = new NeuralNetConfiguration.Builder()
				.graphBuilder()
				.addInputs("input")
		        .addLayer("L1", new LSTM.Builder().nIn(5).nOut(5).build(), "input")
		        .addLayer("L2",new RnnOutputLayer.Builder().nIn(5+5).nOut(5).build(), "input", "L1")
		        .setOutputs("L2")
				.build();
		System.out.println(cgConf1.toJson());
		
		//多输入值及顶点合并
		ComputationGraphConfiguration cgConf2 = new NeuralNetConfiguration.Builder()
				.graphBuilder()
				.addInputs("input1", "input2")
				.addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input1")
		        .addLayer("L2", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input2")
		        .addVertex("merge", new MergeVertex(), "L1", "L2")
		        .addLayer("out", new OutputLayer.Builder().nIn(4+4).nOut(3).build(), "merge")
		        .setOutputs("out")
				.build();
		System.out.println(cgConf2.toJson());
		
		//多任务学习
		ComputationGraphConfiguration cgConf3 = new NeuralNetConfiguration.Builder()
				.graphBuilder()
				.addInputs("input")
		        .addLayer("L1", new DenseLayer.Builder().nIn(3).nOut(4).build(), "input")
		        .addLayer("out1", new OutputLayer.Builder()
		                .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
		                .nIn(4).nOut(3).build(), "L1")
		        .addLayer("out2", new OutputLayer.Builder()
		                .lossFunction(LossFunctions.LossFunction.MSE)
		                .nIn(4).nOut(2).build(), "L1")
		        .setOutputs("out1","out2")
				.build();
		System.out.println(cgConf3.toJson());
		
	}

}
