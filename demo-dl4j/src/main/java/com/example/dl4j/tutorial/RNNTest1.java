package com.example.dl4j.tutorial;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;


public class RNNTest1 {

	public static void main(String[] args) throws Exception {
		Path dataPath = Paths.get(System.getProperty("java.io.tmpdir"), "uci_synthetic_control");
		System.out.println(dataPath.toString());
		if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
			String url = "https://archive.ics.uci.edu/ml/machine-learning-databases/synthetic_control-mld/synthetic_control.data";
			System.out.println("Downloading file...");
			String data = IOUtils.toString(new URL(url), Charset.defaultCharset());
			String[] lines = data.split("\n");
			
			int lineCount = 0;
			int index = 0;
			
			List<String> linesList = new ArrayList<>();
			
			for (String line : lines) {
				Integer count = new Integer(lineCount / 100);
				String newLine = null;
				newLine = line.replaceAll("\\s+", ", " + count.toString() + "\n");
				newLine += ", " + count.toString();
				linesList.add(newLine);
				lineCount += 1;
			}
			
			Collections.shuffle(linesList);
			
			for (String line : linesList) {
				Path outPath = Paths.get(dataPath.toString(), index + ".csv");
				FileUtils.writeStringToFile(outPath.toFile(), line, Charset.defaultCharset());
				index += 1;
			}
			System.out.println("Done");
		} else {
			System.out.println("File already exists");
		}
		
		int batchSize = 128;
		//总共6个类别
		int numLabelClasses = 6;
		
		//训练数据
		CSVSequenceRecordReader trainRR = new CSVSequenceRecordReader(0, ", ");
		trainRR.initialize(new NumberedFileInputSplit(dataPath.toAbsolutePath().toString() + "/%d.csv", 0, 449));
		DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainRR, batchSize, numLabelClasses, 1);
		
		//测试数据
		CSVSequenceRecordReader testRR = new CSVSequenceRecordReader(0, ", ");
		testRR.initialize(new NumberedFileInputSplit(dataPath.toAbsolutePath().toString() + "/%d.csv", 450, 599));
		DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testRR, batchSize, numLabelClasses, 1);
	
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(123)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.weightInit(WeightInit.XAVIER)
				.updater(new Nesterovs(0.005, 0.9))
				.gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
				.gradientNormalizationThreshold(0.5)
				.list()
				.layer(0, new GravesLSTM.Builder().activation(Activation.TANH)
						.nIn(1).nOut(10).build())
				.layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
						.activation(Activation.SOFTMAX).nIn(10).nOut(numLabelClasses).build())
				.pretrain(false).backprop(true).build();
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.setListeners(new ScoreIterationListener(20));
		
		model.fit(trainIter);
		
		Evaluation evaluation = model.evaluate(testIter);
		
		System.out.println("Accuracy: " + evaluation.accuracy());
		System.out.println("Precision: " + evaluation.precision());
		System.out.println("Recall: " + evaluation.recall());
		
	}

}
