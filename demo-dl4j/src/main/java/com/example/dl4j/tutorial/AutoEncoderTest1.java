package com.example.dl4j.tutorial;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
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
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class AutoEncoderTest1 {

	public static void main(String[] args) throws IOException {
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .seed(12345)
	            .weightInit(WeightInit.XAVIER)
	            .updater(new AdaGrad(0.05))
	            .activation(Activation.RELU)
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .l2(0.0001)
	            .list()
	            .layer(0, new DenseLayer.Builder().nIn(784).nOut(250).build())
	            .layer(1, new DenseLayer.Builder().nIn(250).nOut(10).build())
	            .layer(2, new DenseLayer.Builder().nIn(10).nOut(250).build())
	            .layer(3, new OutputLayer.Builder().nIn(250).nOut(784)
            			.lossFunction(LossFunction.MSE)
            			.build())
	            .pretrain(false).backprop(true)
	            .build();
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(1));
		
		//载入50000条数据并且将其分成40000条训练数据，10000条测试数据
		DataSetIterator iter = new MnistDataSetIterator(100,50000,false);
		
		List<INDArray> featuresTrain = new ArrayList<>();
		List<INDArray> featuresTest = new ArrayList<>();
		List<INDArray> labelsTest  = new ArrayList<>();
		
		Random random = new Random(12345);
		
		while (iter.hasNext()) {
			DataSet next = iter.next();
			//从100条数据中分割成按80/20的比例分割
			SplitTestAndTrain split = next.splitTestAndTrain(80, random);
			featuresTrain.add(split.getTrain().getFeatures());
			featuresTest.add(split.getTest().getFeatures());
			//将单热编码转换成数据对应的索引值
			//例如[0,0,0,1,0,0,0,0,0,0]，这里唯一不为0的数值索引为4，因此将这条数据转换成4
			labelsTest.add(Nd4j.argMax(split.getTest().getLabels(), 1));
		}
		
		for (int i = 1; i <= 30; i++) {
			for (INDArray data: featuresTrain) {
				model.fit(data, data);
			}
			System.out.println("Epoch " + i + " complete");
		}
		
		Map<Integer, List<Pair<Double, INDArray>>> listsByDigit = new HashMap<>();
		for (int i = 0; i <= 9; i++) {
			listsByDigit.put(i, new ArrayList<Pair<Double, INDArray>>());
		}
		
		//通过测试数据来评估模型准确度
		for (int i = 0; i <= featuresTest.size() - 1; i++) {
			INDArray testData = featuresTest.get(i);
			INDArray labels = labelsTest.get(i);
			for (int j = 0; j <= testData.rows() - 1; j++) {
				INDArray example = testData.getRow(j);
				int digit = new Double(labels.getDouble(j)).intValue();
				double score = model.score(new DataSet(example, example));
				List<Pair<Double, INDArray>> digitAllPairs = listsByDigit.get(digit);
				digitAllPairs.add(new ImmutablePair<Double, INDArray>(score, example));
			}
		}
		
		Comparator<Pair<Double, INDArray>> c = new Comparator<Pair<Double,INDArray>>() {
			@Override
			public int compare(Pair<Double, INDArray> o1, Pair<Double, INDArray> o2) {
				return java.lang.Double.compare(o1.getLeft(), o2.getLeft());
			}
		};
		
		for (List<Pair<Double, INDArray>> digitAllPairs: listsByDigit.values()) {
			Collections.sort(digitAllPairs, c);
		}
		
		List<INDArray> best = new ArrayList<>(50);
		List<INDArray> worst = new ArrayList<>(50);
		
		for (int i = 0; i <= 9; i++) {
			List<Pair<Double, INDArray>> digitAllPairs = listsByDigit.get(i);
			for (int j = 0; j <= 4; j++) {
				best.add(digitAllPairs.get(j).getRight());
				worst.add(digitAllPairs.get(digitAllPairs.size() - 1 - j).getRight());
			}
		}
		
		System.out.println("<br /><h2>Worst Scoring Digits</h2>");
		worst.stream().map(s -> {
			try {
				String result = "<img src=\"data:image/png;base64," + encodeArrayToImage(s) +
						"\" style=\"float:left; display:block; margin:10px\">";
				return result;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).forEach(System.out::println);
		
		System.out.println("<br /><h2>Best Scoring Digits</h2>");
		best.stream().map(s -> {
			try {
				String result = "<img src=\"data:image/png;base64," + encodeArrayToImage(s) +
						"\" style=\"float:left; display:block; margin:10px\">";
				return result;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).forEach(System.out::println);
	}
	
	public static String encodeArrayToImage(INDArray arr) throws IOException {
		BufferedImage bi = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < 784; i++) {
			bi.getRaster().setSample(i % 28,  i / 28, 0, new Double(255*arr.getDouble(i)).intValue());
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		
		ImageIO.write(bi, "PNG", baos);
		byte[] image = baos.toByteArray();
		baos.close();
		
		return Base64.getEncoder().encodeToString(image);
	}
}
