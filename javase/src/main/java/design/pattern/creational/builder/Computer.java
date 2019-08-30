package design.pattern.creational.builder;

public class Computer {
    private String CPU;
    private String GPU;
    private String motherboard;
    private int RAM;
    private int HDD;
    private int SSD;

    public Computer(Builder builder) {
        this.CPU = builder.CPU;
        this.motherboard = builder.motherboard;
        this.GPU = builder.GPU;
        this.RAM = builder.RAM;
        this.HDD = builder.HDD;
        this.SSD = builder.SSD;
    }

    @Override
    public String toString() {
        return "Computer config: "
                + "CPU: " + this.CPU  + " motherboard: " + this.motherboard + " HDD: " + this.HDD + "G "
                + (this.SSD == 0 ? "" : "SSD: " + this.SSD + "G ")
                + (this.GPU == null ? "" : "GPU: " + this.GPU);
    }

    public static class Builder {
        private String CPU;
        private String GPU;
        private String motherboard;
        private int RAM;
        private int HDD;
        private int SSD;

        public Builder(String CPU, String motherboard, int RAM, int HDD) {
            this.CPU = CPU;
            this.motherboard = motherboard;
            this.RAM = RAM;
            this.HDD = HDD;
        }

        public Builder withGPU(String GPU) {
            this.GPU = GPU;
            return this;
        }

        public Builder withRAM(int RAM) {
            this.RAM += RAM;
            return this;
        }

        public Builder withSSD(int SSD) {
            this.SSD += SSD;
            return this;
        }

        public Builder withHDD(int HDD) {
            this.HDD += HDD;
            return this;
        }

        public Computer build() {
            return new Computer(this);
        }
    }
}
