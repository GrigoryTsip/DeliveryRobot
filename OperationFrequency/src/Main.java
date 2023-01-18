import java.util.*;
import java.util.concurrent.Executor;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {

        final int STRING_LENGHT = 100;
        final int NUMBER_OF_THREADS = 1000;
        final char LETTER = 'R';

        Runnable robot = () -> {

            List<Integer> frequencyR = new ArrayList<>();

            String route = generateRoute("RLRFR", STRING_LENGHT);
            char[] letter = route.toCharArray();

            boolean startR = false;
            int freqR = 0;

            for (char let : letter) {
                if (let == LETTER && !startR) startR = true;
                if (let == LETTER) {
                    freqR++;
                } else {
                    if (startR) {
                        startR = false;
                        frequencyR.add(freqR);
                        freqR = 0;
                    }
                }
            }

            //System.out.print(Thread.currentThread().getId() + " ");
            //System.out.println(frequencyR.toString());

            synchronized (sizeToFreq) {
                for (int len : frequencyR) {
                    if (sizeToFreq.containsKey(len)) {
                        sizeToFreq.put(len, sizeToFreq.get(len) + 1);
                    } else {
                        sizeToFreq.put(len, 1);
                    }
                }
                sizeToFreq.notify();
            }
        };

        Runnable calc = () -> {

            while (!Thread.interrupted()) {

                synchronized (sizeToFreq) {

                    try {
                        sizeToFreq.wait();

                        int maxFreq = 0;
                        int maxVal = 0;
                        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                            int freq = entry.getKey();
                            int val = entry.getValue();
                            if (maxVal < val) {
                                maxVal = val;
                                maxFreq = freq;
                            }
                        }
                        System.out.println("Текущий лидер: " + maxFreq + " (" + maxVal + " раз)");
                    } catch (InterruptedException ignored) {}
                }
            }
        };

        List<Thread> threads = new ArrayList<>();

        Thread calcThread = new Thread(calc);
        calcThread.start();


        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread(robot);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        calcThread.interrupt();

        int maxKey = 0;
        int maxFreq = 0;
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            int val = entry.getValue();
            int key = entry.getKey();
            if (maxFreq < val) {
                maxKey = key;
                maxFreq = val;
            }
        }

        System.out.println("Самое частое количество повторений буквы " + LETTER + ": " + maxKey + " (встретилось " + sizeToFreq.get(maxKey) + " раз)");
        System.out.println("Другие частоты:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            if (entry.getKey() != maxKey)
                System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}