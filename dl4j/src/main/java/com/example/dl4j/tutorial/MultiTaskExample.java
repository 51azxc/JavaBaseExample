package com.example.dl4j.tutorial;

import com.example.dl4j.utilities.DataUtilities;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultiTaskExample {
    public static void main(String[] args) throws Exception {
        // 下载数据
        String DATA_URL = "https://bpstore1.blob.core.windows.net/tutorials/instacart.tar.gz";
        String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_instacart").toString();
        String localFilePath = Paths.get(DATA_PATH, "instacart.tar.gz").toString();
        if (DataUtilities.downloadFile(DATA_URL, localFilePath)) {
            System.out.println("download file from: " + DATA_URL);
        }

        Path dataPath = Paths.get(DATA_PATH, "instacart");
        if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
            DataUtilities.extractTarGz(localFilePath, DATA_PATH);
        }

        // 配置数据集
        String featureBaseDir = Paths.get(dataPath.toString(), "features").toString();
        String targetsBaseDir = Paths.get(dataPath.toString(), "breakfast").toString();
        String auxilBaseDir = Paths.get(dataPath.toString(), "dairy").toString();

        SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(1);
        trainFeatures.initialize(
                new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 1, 4000));
        SequenceRecordReader trainBreakfast = new CSVSequenceRecordReader(1);
        trainBreakfast.initialize(
                new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 1, 4000));
        SequenceRecordReader trainDairy = new CSVSequenceRecordReader(1);
        trainDairy.initialize(
                new NumberedFileInputSplit(auxilBaseDir + "/%d.csv", 1, 4000));
        MultiDataSetIterator train = new RecordReaderMultiDataSetIterator.Builder(20)
                .addSequenceReader("rr1", trainFeatures).addInput("rr1")
                .addSequenceReader("rr2",trainBreakfast).addOutput("rr2")
                .addSequenceReader("rr3",trainDairy).addOutput("rr3")
                .sequenceAlignmentMode(RecordReaderMultiDataSetIterator.AlignmentMode.ALIGN_END)
                .build();

        SequenceRecordReader testFeatures = new CSVSequenceRecordReader(1);
        testFeatures.initialize(
                new NumberedFileInputSplit(featureBaseDir + "/%d.csv", 4001, 5000));
        SequenceRecordReader testBreakfast = new CSVSequenceRecordReader(1);
        testBreakfast.initialize(
                new NumberedFileInputSplit(targetsBaseDir + "/%d.csv", 4001, 5000));
        SequenceRecordReader testDairy = new CSVSequenceRecordReader(1);
        testDairy.initialize(
                new NumberedFileInputSplit(auxilBaseDir + "/%d.csv", 4001, 5000));
        MultiDataSetIterator test = new RecordReaderMultiDataSetIterator.Builder(20)
                .addSequenceReader("rr1", testFeatures).addInput("rr1")
                .addSequenceReader("rr2",testBreakfast).addOutput("rr2")
                .addSequenceReader("rr3",testDairy).addOutput("rr3")
                .sequenceAlignmentMode(RecordReaderMultiDataSetIterator.AlignmentMode.ALIGN_END)
                .build();

        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).seed(12345)
                //.dropOut(0.25)
                .graphBuilder()
                .addInputs("input")
                .addLayer("L1", new LSTM.Builder()
                        .nIn(134).nOut(150)
                        .updater(new Adam())
                        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                        .gradientNormalizationThreshold(10)
                        .activation(Activation.TANH)
                        .build(), "input")
                .addLayer("out1", new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .updater(new Adam())
                        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                        .gradientNormalizationThreshold(10)
                        .activation(Activation.SIGMOID)
                        .nIn(150).nOut(1).build(), "L1")
                .addLayer("out2", new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .updater(new Adam())
                        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                        .gradientNormalizationThreshold(10)
                        .activation(Activation.SIGMOID)
                        .nIn(150).nOut(1).build(), "L1")
                .setOutputs("out1","out2")
                .pretrain(false).backprop(true)
                .build();

        ComputationGraph model = new ComputationGraph(conf);
        model.init();

        for (int i = 1; i <= 5; i++) {
            System.out.println("Epoch " + i);
            model.fit(train);
            train.reset();
        }

        ROC roc = new ROC();
        test.reset();

        while(test.hasNext()){
            MultiDataSet next = test.next();
            INDArray[] features =  next.getFeatures();
            INDArray[] output = model.output(features[0]);
            roc.evalTimeSeries(next.getLabels()[0], output[0]);
        }

        System.out.println(roc.calculateAUC());
    }
}
