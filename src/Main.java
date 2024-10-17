import java.util.Arrays;

public class Main {
    private static final int SIZE = 23;
    private static final int HALF = SIZE / 2;
    private static int threadCount = 4;

    public static void main(String[] args) {
        float[] thread1, thread2, threads;

        long startTime, endTime;

        // время 1 метода
        startTime = System.currentTimeMillis();
        thread1 = methodThread1(); // метод с однопоточными вычислениями
        endTime = System.currentTimeMillis();
        long time1 = endTime - startTime;

        // время 2 метода
        startTime = System.currentTimeMillis();
        thread2 = methodThread2(); // двухпоточный метод
        endTime = System.currentTimeMillis();
        long time2 = endTime - startTime;

        // время 3 метода
        startTime = System.currentTimeMillis();
        threads = methodThread3();  // метод, где можно менять количество потоков
        endTime = System.currentTimeMillis();
        long time3 = endTime - startTime;

        // вывод времени
        System.out.printf("Время выполнения:\n1 методом: %d мс\n2 методом: %d мс\n3 методом: %d мс\n\n", time1, time2, time3);

        // вывод длины подмассивов для 3 метода
        int partSize = SIZE / threadCount;
        System.out.printf("Длина подмассивов для 3 метода:\n");
        for (int i = 1; i <= threadCount; i++) {
            System.out.printf("Поток %d: %d элементов\n", i, i == 1 ? partSize + (SIZE % threadCount) : partSize);
        }

        // вывод результатов
        System.out.printf("\nРезультаты вычислений:\nПервый элемент:\n1 метод: %.8f\n2 метод: %.8f\n3 метод: %.8f\n\n",
                thread1[0], thread2[0], threads[0]);

        System.out.printf("Последний элемент:\n1 метод: %.8f\n2 метод: %.8f\n3 метод: %.8f\n",
                thread1[SIZE - 1], thread2[SIZE - 1], threads[SIZE - 1]);
    }

    private static float[] methodThread3() {
        float[] array = new float[SIZE];
        Arrays.fill(array, 1);

        int partSize = SIZE / threadCount;
        int remainder = SIZE % threadCount;

        Thread[] threads = new Thread[threadCount];
        float[][] parts = new float[threadCount][];
        int sourcePosition = 0;

        for (int i = 0; i < threadCount; i++) {
            float[] arrayPart = new float[partSize + (i == 0 ? remainder : 0)];
            System.arraycopy(array, sourcePosition, arrayPart, 0, arrayPart.length);

            int offset = sourcePosition;
            int finalI = i;
            sourcePosition += arrayPart.length;

            threads[i] = new Thread(() -> {
                for (int j = 0; j < arrayPart.length; j++) {
                    arrayPart[j] = calculateValue(j + offset);
                }
                parts[finalI] = arrayPart;
            });

            threads[i].start();
        }

        sourcePosition = 0;

        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.arraycopy(parts[i], 0, array, sourcePosition, parts[i].length);
            sourcePosition += parts[i].length;

            // вывод элементов потока
            System.out.printf("Поток %d содержит элементы: %s\n", i, Arrays.toString(parts[i]));
        }

        return array;
    }

    private static float[] methodThread2() {
        float[] array = new float[SIZE];
        Arrays.fill(array, 1);

        float[] firstHalf = new float[HALF];
        float[] secondHalf = new float[SIZE - HALF];

        System.arraycopy(array, 0, firstHalf, 0, HALF);
        System.arraycopy(array, HALF, secondHalf, 0, SIZE - HALF);

        Thread firstHalfCalculation = new Thread(() -> {
            for (int i = 0; i < HALF; i++) {
                firstHalf[i] = calculateValue(i);
            }
        });

        Thread secondHalfCalculation = new Thread(() -> {
            for (int i = 0; i < secondHalf.length; i++) {  // Цикл по длине второй части
                secondHalf[i] = calculateValue(i + HALF);  // Корректный индекс
            }
        });

        firstHalfCalculation.start();
        secondHalfCalculation.start();

        try {
            firstHalfCalculation.join();
            secondHalfCalculation.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.arraycopy(firstHalf, 0, array, 0, HALF);
        System.arraycopy(secondHalf, 0, array, HALF, SIZE - HALF);  // Обратное копирование

        // Вывод элементов обоих потоков
        System.out.printf("Первый поток содержит элементы: %s\n", Arrays.toString(firstHalf));
        System.out.printf("Второй поток содержит элементы: %s\n", Arrays.toString(secondHalf));

        return array;
    }

    private static float[] methodThread1() {
        float[] array = new float[SIZE];
        Arrays.fill(array, 1);

        for (int i = 0; i < SIZE; i++) {
            array[i] = calculateValue(i);
        }

        return array;
    }

    private static float calculateValue(int index) {
        return (float) (Math.sin(0.2f + index / 5) * Math.cos(0.2f + index / 5) * Math.cos(0.4f + index / 2));
    }
}
