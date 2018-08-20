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

import com.example.dl4j.utilities.DataUtilities;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.SequenceRecordReader;
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
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.preprocessor.CnnToRnnPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.RnnToCnnPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class CLSTMExample2 {

	public static void main(String[] args) throws Exception {
		// 下载数据
		String DATA_URL = "https://bpstore1.blob.core.windows.net/seatemp/sea_temp2.tar.gz";
        String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_seas2").toString();
        String localFilePath = Paths.get(DATA_PATH, "sea_temp.tar.gz").toString();
        if (DataUtilities.downloadFile(DATA_URL, localFilePath)) {
            System.out.println("download file from: " + DATA_URL);
        }

        Path dataPath = Paths.get(DATA_PATH, "sea_temp");
        if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
            DataUtilities.extractTarGz(localFilePath, DATA_PATH);
        }

		// 配置数据集
        String featureBaseDir = Paths.get(dataPath.toString(), "features").toString();
        String targetsBaseDir = Paths.get(dataPath.toString(), "targets").toString();
        String futureBaseDir = Paths.get(dataPath.toString(), "futures").toString();
		
		int numSkipLines = 1;
		boolean regression = true;
		int batchSize = 32;

		// 训练数据
		CSVSequenceRecordReader trainFeatures = new CSVSequenceRecordReader(numSkipLines);
		trainFeatures.initialize(
				new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 1, 1600));
		CSVSequenceRecordReader trainTargets = new CSVSequenceRecordReader(numSkipLines);
		trainTargets.initialize(
				new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 1, 1600));
		DataSetIterator train = new SequenceRecordReaderDataSetIterator(trainFeatures, trainTargets, batchSize, 10,
				regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);

		// 测试数据
        SequenceRecordReader testFeatures = new CSVSequenceRecordReader(numSkipLines);
		testFeatures.initialize(
				new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 1601, 1736));
        SequenceRecordReader testTargets = new CSVSequenceRecordReader(numSkipLines);
		testTargets.initialize(
				new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 1601, 1736));
		DataSetIterator test = new SequenceRecordReaderDataSetIterator(testFeatures, testTargets, batchSize, 10,
				regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);

        SequenceRecordReader futureFeatures = new CSVSequenceRecordReader(numSkipLines);
		futureFeatures.initialize(
				new NumberedFileInputSplit(futureBaseDir + "/%d.csv", 1601, 1736));
        SequenceRecordReader futureLabels = new CSVSequenceRecordReader(numSkipLines);
		futureLabels.initialize(
				new NumberedFileInputSplit(futureBaseDir + "/%d.csv", 1601, 1736));
		DataSetIterator future = new SequenceRecordReaderDataSetIterator(futureFeatures, futureLabels, batchSize, 10,
				regression, SequenceRecordReaderDataSetIterator.AlignmentMode.EQUAL_LENGTH);

		// 配置神经网络
		int V_HEIGHT = 13;
		int V_WIDTH = 4;
		int kernelSize = 2;
		int numChannels = 1;

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).seed(12345)
				.updater(new Adam()).weightInit(WeightInit.XAVIER).list()
				.layer(0, new ConvolutionLayer.Builder(kernelSize, kernelSize).updater(new AdaGrad()).nIn(numChannels)
						.nOut(7).stride(2, 2).activation(Activation.RELU).build())
				.layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
				        .kernelSize(kernelSize, kernelSize)
				        .stride(2, 2).build())
				    .layer(2, new LSTM.Builder()
				        .activation(Activation.SOFTSIGN)
				        .nIn(21)
				        .nOut(100)
				        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
				        .gradientNormalizationThreshold(10)
				        .build())
				    .layer(3, new RnnOutputLayer.Builder(LossFunction.MSE)
				        .activation(Activation.IDENTITY)
				        .nIn(100)
				        .nOut(52)
				        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
				        .gradientNormalizationThreshold(10)
				        .build())
				    .inputPreProcessor(0, new RnnToCnnPreProcessor(V_HEIGHT, V_WIDTH, numChannels))
				    .inputPreProcessor(2, new CnnToRnnPreProcessor(3, 1, 7 ))
				    .pretrain(false).backprop(true)
				    .build();

		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();

		for (int i = 1; i <= 15; i++) {
			System.out.println("Epoch " + i);
			model.fit(train);
			train.reset();
		}

		RegressionEvaluation eval = new RegressionEvaluation();
		test.reset();
		future.reset();
		while(test.hasNext()) {
			DataSet next = test.next();
			INDArray features = next.getFeatures();
			INDArray pred = Nd4j.zeros(1, 2);
			
			for (int i = 0; i <= 49; i++) {
				pred = model.rnnTimeStep(features.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(i,i+1)));
			}
			
			DataSet correct = future.next();
			INDArray cFeatures = correct.getFeatures();
			
			for (int i = 0; i <= 9; i++) {
				eval.evalTimeSeries(pred, cFeatures.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(i,i+1)));
				pred = model.rnnTimeStep(pred);
			}
			model.rnnClearPreviousState();
		}
		System.out.println(eval.stats());
	}

}
