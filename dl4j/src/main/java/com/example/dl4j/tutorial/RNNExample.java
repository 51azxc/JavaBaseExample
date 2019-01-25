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
import java.util.Random;

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
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;


public class RNNExample {

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
				//文件默认是以空格分割数据的，现在讲分隔符变成", "，然后列转行
				newLine = line.replaceAll("\\s+", ", " + count.toString() + "\n");
				//结尾追加分割符与标签值
				newLine += ", " + count.toString();
				linesList.add(newLine);
				lineCount += 1;
			}

			//将数据随机打乱，不然都是统一的数据无法测试
			Collections.shuffle(linesList, new Random(12345));
			
			for (String line : linesList) {
				//将数据写入到csv文件中
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
		//指定分隔符为", "
		CSVSequenceRecordReader trainRR = new CSVSequenceRecordReader(0, ", ");
		//NumberedFileInputSplit可以通过占位符来读取一堆csv文件
		trainRR.initialize(
				new NumberedFileInputSplit(dataPath.toAbsolutePath().toString() + "/%d.csv", 0, 449));
		//指定标签索引为1
		DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainRR, batchSize, numLabelClasses, 1);

		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(trainIter);
        trainIter.reset();

        trainIter.setPreProcessor(normalizer);

		//测试数据
		CSVSequenceRecordReader testRR = new CSVSequenceRecordReader(0, ", ");
		testRR.initialize(
				new NumberedFileInputSplit(dataPath.toAbsolutePath().toString() + "/%d.csv", 450, 599));
		DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testRR, batchSize, numLabelClasses, 1);

        testIter.setPreProcessor(normalizer);
	
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(123)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.weightInit(WeightInit.XAVIER)
				//学习速率为0.005
				.updater(new Nesterovs(0.005, 0.9))
				.gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
				.gradientNormalizationThreshold(0.5)
				.list()
				.layer(0, new LSTM.Builder().activation(Activation.TANH)
						.nIn(1).nOut(60).build())
				.layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
						.activation(Activation.SOFTMAX).nIn(60).nOut(numLabelClasses).build())
				.pretrain(false).backprop(true).build();
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.setListeners(new ScoreIterationListener(20));

		for (int i = 0; i < 40; i++) {
            model.fit(trainIter);
		}

		Evaluation evaluation = model.evaluate(testIter);
		
		System.out.println("Accuracy: " + evaluation.accuracy());
		System.out.println("Precision: " + evaluation.precision());
		System.out.println("Recall: " + evaluation.recall());
		
	}

}
