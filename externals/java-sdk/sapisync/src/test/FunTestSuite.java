import junit.framework.*;

public class FunTestSuite extends TestSuite {
    public static Test suite() {
        FunTestSuite suite = new FunTestSuite();
        suite.addTest(new com.funambol.sapisync.SapiSyncAnchorTest("testParse"));
        suite.addTest(new com.funambol.sapisync.SapiSyncAnchorTest("testFormat"));
        suite.addTest(new com.funambol.sapisync.SapiSyncAnchorTest("testReset"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullUpload"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testIncrementalUpload"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullDownload1"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullDownload2"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testIncrementalDownload1"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testIncrementalDownload2"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullUploadFilter_ItemsCount"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testIncrementalUploadFilter_ItemsCount"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullDownloadFilter_ItemsCount"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullDownloadFilter_ItemsCount2"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTest("testFullDownloadFilter_DateRecent"));
        suite.addTest(new com.funambol.sapisync.SapiSyncManagerTwinTest("testTwinDetection"));
        suite.addTest(new com.funambol.sapisync.sapi.SapiHandlerTest("testAuthentication1"));
        suite.addTest(new com.funambol.sapisync.sapi.SapiHandlerTest("testAuthentication2"));
        suite.addTest(new com.funambol.sapisync.sapi.SapiHandlerTest("testAuthentication3"));
        suite.addTest(new com.funambol.sapisync.sapi.SapiHandlerTest("testSapi1"));
        suite.addTest(new com.funambol.sapisync.source.CacheTrackerWithRenamesTest("testRename"));
        suite.addTest(new com.funambol.sapisync.source.CacheTrackerWithRenamesTest("testRenameAndUpdate"));
        suite.addTest(new com.funambol.sapisync.source.CacheTrackerWithRenamesTest("testEmpty"));
        suite.addTest(new com.funambol.sapisync.source.CacheTrackerWithRenamesTest("testReset"));
        return suite;
    }
}
