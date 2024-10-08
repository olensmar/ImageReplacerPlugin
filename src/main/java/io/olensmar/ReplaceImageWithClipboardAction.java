// Copyright 2024 Ole Lensmar
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.olensmar;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ReplaceImageWithClipboardAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();

        if (file != null && file.getName().toLowerCase().endsWith(".png")) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
                    BufferedImage image = getBufferedImage((Image) clipboard.getData(DataFlavor.imageFlavor));

                    int result = Messages.showYesNoDialog(
                            project,
                            "Replace image in file [" + file.getName() + "] with clipboard image?",
                            "Confirmation",
                            getMessageIcon(image, 250)
                    );

                    if (result == Messages.YES) {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                                ImageIO.write(image, "png", outputStream);
                                file.setBinaryContent(outputStream.toByteArray());
                                file.refresh(false, false);
                            } catch (Exception ex) {
                                System.out.println("ReplaceImageWithClipboardAction: Failed to update file: " + ex.getMessage());
                            }
                        });
                    }
                } else {
                    System.out.println("ReplaceImageWithClipboardAction: No image in clipboard.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("ReplaceImageWithClipboardAction: File is not png.");
        }
    }

    private static @NotNull ImageIcon getMessageIcon(BufferedImage image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if( width > maxSize || height > maxSize ){
            if( width > height ){
                height = height * maxSize / width;
                width = maxSize;
            }
            else {
                width = width * maxSize / height;
                height = maxSize;
            }
        }

        return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public static BufferedImage getBufferedImage(Image img) {
        if (img instanceof MultiResolutionImage) {
            MultiResolutionImage multiResolutionImage = (MultiResolutionImage) img;
            List<Image> images = multiResolutionImage.getResolutionVariants();
            Image image = images.get(0);

            // find largest image
            if (images.size() > 1) {
                for (int c = 1; c < images.size(); c++) {
                    if (images.get(c).getHeight(null) * images.get(c).getWidth(null) > image.getHeight(null) * image.getWidth(null)) {
                        image = images.get(c);
                    }
                }
            }

            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            } else {
                // Handle other image types if necessary
                throw new IllegalArgumentException("Resolution variant is not a BufferedImage");
            }
        }
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        throw new IllegalArgumentException("Image is not a MultiResolutionCachedImage or BufferedImage");
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isPngFile = file != null && file.getName().endsWith(".png");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        e.getPresentation().setVisible(isPngFile);
        e.getPresentation().setEnabled(isPngFile && clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}