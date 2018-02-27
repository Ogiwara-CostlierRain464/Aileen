package jp.ogiwara.java.aileen;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.SparseArray;

import org.junit.Test;
import org.junit.runner.RunWith;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void youtubeExtractorTest() throws Exception {
        String link = "https://www.youtube.com/watch?v=zTmlrZw6Ctc";

        Context context = InstrumentationRegistry.getTargetContext();

        new YouTubeExtractor(context){
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                assertNotNull(ytFiles);
            }
        }.extract(link, true, true);
    }
}
