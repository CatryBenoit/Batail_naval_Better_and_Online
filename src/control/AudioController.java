package control;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioController {
    private Clip audioClip;
    private String audioPath = "src/Elements/Audio/";

    private void play(String fileName) {
        String filePath = audioPath + fileName;
        try {
            // le son est cherché dans le classpath (comme les images), sinon dans src/ depuis la racine du projet
            URL audioUrl = AudioController.class.getResource("/Elements/Audio/" + fileName);
            if (audioUrl == null) {
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    System.err.println("File not found: " + filePath);
                    return;
                }
                audioUrl = audioFile.toURI().toURL();
            }
            // on libère le son précédent avant d'en jouer un nouveau
            stop();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioUrl);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            audioClip.start();
            System.out.println("Playing: " + fileName);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
            // IllegalArgumentException : aucune sortie son (par exemple dans un conteneur Docker)
            System.err.println("Error playing the file: " + fileName);
            e.printStackTrace();
        }
    }

    public void playMiss() {
        play("miss.wav");
    }

    public void stop() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
    }

    public void listAudioFiles() {
        File directory = new File(audioPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory not found: " + audioPath);
            return;
        }

        try {
            Files.list(Paths.get(audioPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> System.out.println(path.getFileName().toString()));
        } catch (IOException e) {
            System.err.println("Error listing audio files.");
            e.printStackTrace();
        }
    }
}
