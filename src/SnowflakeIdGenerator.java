public class SnowflakeIdGenerator {

    private final long twepoch = 1672531200000L; // Custom epoch (e.g., Jan 1, 2023)
    private final long machineIdBits = 10L;
    private final long maxMachineId = -1L ^ (-1L << machineIdBits);
    private final long sequenceBits = 12L;

    private final long machineIdShift = sequenceBits;
    private final long timestampShift = sequenceBits + machineIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final long machineId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(long machineId) {
        if (machineId > maxMachineId || machineId < 0) {
            throw new IllegalArgumentException("Machine ID out of range");
        }
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long timestamp = currentTime();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampShift)
                | (machineId << machineIdShift)
                | sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= currentTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);
        for (int i = 0; i < 10; i++) {
            System.out.println(generator.nextId());
        }
    }
}