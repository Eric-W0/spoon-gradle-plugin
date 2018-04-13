/*
 * Copyright 2018 eb2501@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eb2501.spoon.gradle;

import spoon.reflect.factory.Factory;
import spoon.support.gui.SpoonModelTree;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class SpoonGui extends SpoonModelTree implements WindowListener {

    public static void show(final Factory factory) {
        final SpoonGui[] placeholder = new SpoonGui[1];
        SwingUtilities.invokeLater(() -> placeholder[0] = new SpoonGui(factory));
        try {
            while (placeholder[0] == null) {
                Thread.sleep(1);
            }
            placeholder[0].waitForClose();
        }
        catch (final InterruptedException e) {}
    }

    private SpoonGui(final Factory factory) {
        super(factory);
    }

    private synchronized void waitForClose() throws InterruptedException {
        wait();
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {}

    @Override
    public synchronized void windowClosed(WindowEvent e) {
        notifyAll();
    }

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}
