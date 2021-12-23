package gvdecoder;
import javax.sound.sampled.*;
import java.io.*;

/**
 * modified from
 * author: www.codejava.net
 */
public class SoundClipRecorder implements Runnable {
    // record duration, in milliseconds
    static final long RECORD_TIME = 60000;  // 1 minute

    // path of the wav file
    File wavFile;

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine line;

    public SoundClipRecorder(String path){
		wavFile=new File(path);
	}

    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }

    /**
     * Captures the sound and record into a WAV file
     */
    void _startrecord() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording...");

            // start recording
            AudioSystem.write(ais, fileType, wavFile);

            System.out.println("after write...");

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    void stop() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }

    public void run(){
		_startrecord();
	}

	public void start(){
		Thread thread=new Thread(this);
		thread.start();
	}
}