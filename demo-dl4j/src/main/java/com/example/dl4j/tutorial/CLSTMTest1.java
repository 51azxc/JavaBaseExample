package com.example.dl4j.tutorial;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.preprocessor.CnnToRnnPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.RnnToCnnPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class CLSTMTest1 {

	public static void main(String[] args) throws Exception {
		
		//下载数据
		String DATA_URL = "https://bpstore1.blob.core.windows.net/seatemp/sea_temp.tar.gz";
		Path DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_seas");
		
		if (Files.notExists(DATA_PATH, LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectory(DATA_PATH);
		}
		
		Path archizePath = Paths.get(DATA_PATH.toString(), "sea_temp.tar.gz");
		if (Files.notExists(archizePath, LinkOption.NOFOLLOW_LINKS)) {
			FileUtils.copyURLToFile(new URL(DATA_URL), archizePath.toFile());
			int fileCount = 0;
			int BUFFER_SIZE = 4096;
			
			try (TarArchiveInputStream tais = new TarArchiveInputStream(
					new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(archizePath.toString()))))) {
				TarArchiveEntry entry;
				while ((entry = (TarArchiveEntry) tais.getNextEntry()) != null) {
					Path entryPath = Paths.get(DATA_PATH.toString(), entry.getName());
					if (entry.isDirectory()) {
						Files.createDirectory(entryPath);
						fileCount = 0;
					} else {
						int count;
						byte[] bytes = new byte[4 * BUFFER_SIZE];
						FileOutputStream fos = new FileOutputStream(entryPath.toFile());
						BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);
						while ((count = tais.read(bytes, 0, BUFFER_SIZE)) != -1) {
							dest.write(bytes, 0, count);
						}
						dest.close();
						fileCount += 1;
					}
					if (fileCount % 1000 == 0) {
						System.out.println(".");
					}
				}
			}
		}
		
		
		//配置数据集
		Path featureBaseDir = Paths.get(DATA_PATH.toString(), "sea_temp", "features");
		Path targetsBaseDir = Paths.get(DATA_PATH.toString(), "sea_temp", "targets");
		int numSkipLines = 1;
		boolean regression = true;
		int batchSize = 32;
		
		//训练数据
		CSVSequenceRecordReader trainFeatures = new CSVSequenceRecordReader(numSkipLines, ", ");
		trainFeatures.initialize(new NumberedFileInputSplit(featureBaseDir.toAbsolutePath().toString() + "/%d.csv", 1, 1936));
		CSVSequenceRecordReader trainTargets = new CSVSequenceRecordReader(numSkipLines, ",");
		trainTargets.initialize(new NumberedFileInputSplit(targetsBaseDir.toAbsolutePath().toString() + "/%d.csv", 1, 1936));
		
		DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainFeatures, trainTargets, batchSize,
                10, regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);
		
		//测试数据
		CSVSequenceRecordReader testFeatures = new CSVSequenceRecordReader(numSkipLines, ", ");
		testFeatures.initialize(new NumberedFileInputSplit(featureBaseDir.toAbsolutePath().toString() + "/%d.csv", 1937, 2089));
		CSVSequenceRecordReader testTargets = new CSVSequenceRecordReader(numSkipLines, ",");
		testTargets.initialize(new NumberedFileInputSplit(targetsBaseDir.toAbsolutePath().toString() + "/%d.csv", 1937, 2089));
		
		DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testFeatures, testTargets, batchSize,
                10, regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);
		
		//配置神经网络
		int V_HEIGHT = 13;
		int V_WIDTH = 4;
		int kernelSize = 2;
		int numChannels = 1;
		
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new ConvolutionLayer.Builder(kernelSize, kernelSize)
                        .updater(new AdaGrad())
                        .nIn(numChannels)
                        .nOut(7)
                        .stride(2, 2)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new LSTM.Builder()
                        .activation(Activation.SOFTSIGN)
                        .nIn(84)
                        .nOut(200)
                        .updater(new AdaGrad())
                        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                        .gradientNormalizationThreshold(10)
                        .build())
                .layer(2, new RnnOutputLayer.Builder(LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(200)
                        .updater(new AdaGrad())
                        .nOut(52)
                        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                        .gradientNormalizationThreshold(10)
                        .build())
                .inputPreProcessor(0, new RnnToCnnPreProcessor(V_HEIGHT, V_WIDTH, numChannels))
                .inputPreProcessor(1, new CnnToRnnPreProcessor(6, 2, 7 ))
                .pretrain(false).backprop(true)
                .build();
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.setListeners(new ScoreIterationListener(20));
		
		for (int i = 1; i <= 25; i++) {
			System.out.println("Epoch " + i);
			model.fit(trainIter);
			trainIter.reset();
		}
		
		RegressionEvaluation eval = model.evaluateRegression(testIter);
		testIter.reset();
		System.out.println(eval.stats());
		
	}

}
