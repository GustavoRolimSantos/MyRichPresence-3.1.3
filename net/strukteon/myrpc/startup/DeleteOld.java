package net.strukteon.myrpc.startup;

import java.io.*;

public class DeleteOld
{
    public static void checkArgs(final String[] args) {
        for (final String s : args) {
            if (s.startsWith("delete ")) {
                final File oldVersion = new File(s.replaceFirst("delete ", ""));
                final File file = null;
                new Thread(() -> {
                    while (file.exists()) {
                        if (!file.delete()) {
                            try {
                                Thread.sleep(100L);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return;
                }).start();
            }
        }
    }
}
