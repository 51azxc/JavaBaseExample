package com.example.dl4j.tutorial;

import com.example.dl4j.utilities.DataUtilities;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LSTMExample {
    public static void main(String[] args) throws Exception {
        // 下载数据
        String DATA_URL = "https://skymindacademy.blob.core.windows.net/physionet2012/physionet2012.tar.gz";
        String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_physionet2012").toString();
        String localFilePath = Paths.get(DATA_PATH, "physionet2012.tar.gz").toString();
        if (DataUtilities.downloadFile(DATA_URL, localFilePath)) {
            System.out.println("download file from: " + DATA_URL);
        }

        Path dataPath = Paths.get(DATA_PATH, "physionet2012");
        if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
            DataUtilities.extractTarGz(localFilePath, DATA_PATH);
        }

        //训练数据集大小
        int NB_TRAIN_EXAMPLES = 3200;
        //测试数据集大小
        int NB_TEST_EXAMPLES = 800;


        // 配置数据集
        // 特征(输入)数据集路径
        String featureBaseDir = Paths.get(dataPath.toString(), "sequence").toString();
        // 标签(输出)数据集路径
        String mortalityBaseDir  = Paths.get(dataPath.toString(), "mortality").toString();

        //跳过第一行，因为第一行是表头。默认的分隔符就为","
        SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(1, ",");
        trainFeatures.initialize(
                new NumberedFileInputSplit(featureBaseDir + "/%d.csv",
                        0, NB_TRAIN_EXAMPLES - 1));

        SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
        trainLabels.initialize(
                new NumberedFileInputSplit(mortalityBaseDir + "/%d.csv",
                        0, NB_TRAIN_EXAMPLES - 1));

        DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels,
                32, 2, false,
                SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        //测试数据
        SequenceRecordReader testFeatures = new CSVSequenceRecordReader(1, ",");
        testFeatures.initialize(
                new NumberedFileInputSplit(featureBaseDir + "/%d.csv",
                        NB_TRAIN_EXAMPLES, NB_TRAIN_EXAMPLES + NB_TEST_EXAMPLES - 1));

        SequenceRecordReader testLabels = new CSVSequenceRecordReader();
        testLabels.initialize(
                new NumberedFileInputSplit(mortalityBaseDir + "/%d.csv",
                        NB_TRAIN_EXAMPLES, NB_TRAIN_EXAMPLES + NB_TEST_EXAMPLES - 1));

        DataSetIterator testData = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels,
                32, 2, false,
                SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        int NB_INPUTS = 86;
        double LEARNING_RATE = 0.005;
        int LSTM_LAYER_SIZE = 200;
        int NUM_LABEL_CLASSES = 2;

        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(1234)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(LEARNING_RATE))
                //.dropOut(0.25)
                .l2(0.01)
                .graphBuilder()
                .addInputs("trainFeatures")
                .setOutputs("predictMortality")
                .addLayer("L1", new LSTM.Builder()
                        .nIn(NB_INPUTS).nOut(LSTM_LAYER_SIZE)
                        .forgetGateBiasInit(1).activation(Activation.RELU)
                        .build(), "trainFeatures")
                .addLayer("predictMortality", new RnnOutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nIn(LSTM_LAYER_SIZE).nOut(NUM_LABEL_CLASSES)
                        .activation(Activation.SOFTMAX).build(), "L1")
                .pretrain(false).backprop(true)
                .build();

        ComputationGraph model = new ComputationGraph(conf);
        model.init();

        DataSetPreProcessor proc = new LastStepPreProc();
        for (int i = 1; i <= 10; i++) {
            model.fit(trainData);
            trainData.reset();
        }

        ROC roc = new ROC(100);
        while(testData.hasNext()){
            DataSet batch = testData.next();
            INDArray[] output = model.output(batch.getFeatures());
            roc.evalTimeSeries(batch.getLabels(), output[0]);
        }
        System.out.println("FINAL TEST AUC: " + roc.calculateAUC());
    }

    static class LastStepPreProc implements DataSetPreProcessor {
        @Override
        public void preProcess(DataSet toPreProcess) {
            INDArray originLabels = toPreProcess.getLabels();
            INDArray lMask = toPreProcess.getLabelsMaskArray();

            INDArray labels2d = pullLastTimeSteps(originLabels, lMask);

            toPreProcess.setLabels(labels2d);
            toPreProcess.setLabelsMaskArray(null);
        }
        public INDArray pullLastTimeSteps(INDArray pullFrom, INDArray mask) {
            if (mask == null) {
                //没有mask数组就取最后一列填充
                long lastTS = pullFrom.size(2) - 1;
                INDArray out = pullFrom.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(lastTS));
                return out;
            } else {
                long[] outShape = new long[2];
                outShape[0] = pullFrom.size(0);
                outShape[1] = pullFrom.size(1);

                INDArray out = Nd4j.create(outShape);
                INDArray lastStepArr = BooleanIndexing.lastIndex(mask, Conditions.epsNotEquals(0.0), 1);
                int[] fwdPassTimeSteps = lastStepArr.data().asInt();
                for (int i = 0; i < fwdPassTimeSteps.length - 1; i++) {
                    out.putRow(i, pullFrom.get(NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(fwdPassTimeSteps[i])));
                }
                return out;
            }
        }
    }
}
