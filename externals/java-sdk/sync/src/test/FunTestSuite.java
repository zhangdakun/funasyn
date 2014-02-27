import junit.framework.*;

public class FunTestSuite extends TestSuite {
    public static Test suite() {
        FunTestSuite suite = new FunTestSuite();
        suite.addTest(new com.funambol.sync.client.CacheTrackerTest("testSimpleFasts"));
        suite.addTest(new com.funambol.sync.client.CacheTrackerTest("testSlowSync1"));
        suite.addTest(new com.funambol.sync.client.CacheTrackerTest("testChangesDuringSync1"));
        suite.addTest(new com.funambol.sync.client.ConfigSyncSourceTest("testBeginSync"));
        suite.addTest(new com.funambol.sync.client.ConfigSyncSourceTest("testSlowSyncSimple"));
        suite.addTest(new com.funambol.sync.client.ConfigSyncSourceTest("testFastSync1"));
        suite.addTest(new com.funambol.sync.client.OTAConfigParserTest("testParseMessageMailSection"));
        suite.addTest(new com.funambol.sync.client.OTAConfigParserTest("testParseMessageContactSection"));
        suite.addTest(new com.funambol.sync.client.OTAConfigParserTest("testParseMessageCalendarSection"));
        suite.addTest(new com.funambol.sync.client.OTAConfigParserTest("testParseMessageTaskSection"));
        suite.addTest(new com.funambol.sync.client.OTAConfigParserTest("testParseMessageNoteUriSection"));
        suite.addTest(new com.funambol.sync.client.OTAConfigParserTest("testParseMessageBriefcaseSection"));
        suite.addTest(new com.funambol.sync.client.RawFileSyncSourceTest("testSlowSyncSimple"));
        suite.addTest(new com.funambol.sync.client.RawFileSyncSourceTest("testRound1"));
        suite.addTest(new com.funambol.sync.client.RawFileSyncSourceTest("testSyncSendLargeObject"));
        return suite;
    }
}
