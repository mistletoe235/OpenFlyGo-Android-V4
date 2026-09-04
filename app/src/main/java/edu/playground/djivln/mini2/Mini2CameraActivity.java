package edu.playground.djivln.mini2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import edu.playground.djivln.vln.AoaUsbProbe;
import edu.playground.djivln.vln.InferenceControlClient;
import edu.playground.djivln.vln.ModelEndpoint;
import edu.playground.djivln.vln.ModelTransport;
import edu.playground.djivln.vln.RemoteInferenceControlClient;
import edu.playground.djivln.camera.SurveyFrameExifWriter;
import edu.playground.djivln.camera.TriggerFrameFreshnessPolicy;
import edu.playground.djivln.camera.TriggerFrameMetadata;
import edu.playground.djivln.reconstruction.V86StreamingController;
import edu.playground.djivln.reconstruction.V86SessionConfig;
import edu.playground.djivln.reconstruction.V86Result;
import edu.playground.djivln.reconstruction.V86PointCloudActivity;
import edu.playground.djivln.reconstruction.V86OfflineImageCompressor;
import edu.playground.djivln.reconstruction.V86PointCloudCachePolicy;
import edu.playground.djivln.reconstruction.V86WorkflowPolicy;
import edu.playground.djivln.reconstruction.V86WorkflowState;
import edu.playground.djivln.hil.AndroidHilController;
import edu.playground.djivln.hil.HilConnectionMode;
import edu.playground.djivln.hil.HilLoopbackPeer;
import edu.playground.djivln.hil.HilOfflineRegressionRunner;
import edu.playground.djivln.hil.HilFrameProtocol;
import edu.playground.djivln.hil.HilProtocol;
import edu.playground.djivln.survey.CameraProfile;
import edu.playground.djivln.survey.DjiCameraProfileCatalog;
import edu.playground.djivln.survey.ChinaCoordinateTransform;
import edu.playground.djivln.survey.GeoPoint;
import edu.playground.djivln.survey.GeoTiffTerrain;
import edu.playground.djivln.survey.GlobalTerrainDownloadResult;
import edu.playground.djivln.survey.GlobalTerrainDownloader;
import edu.playground.djivln.survey.BuildingHeightDownloadResult;
import edu.playground.djivln.survey.CompositeSurfaceElevationSource;
import edu.playground.djivln.survey.GlobalBuildingHeightDownloader;
import edu.playground.djivln.survey.SurveyTerrainPlanner;
import edu.playground.djivln.survey.SurveyTerrainPlanResult;
import edu.playground.djivln.survey.SurveyTerrainSafetyReport;
import edu.playground.djivln.survey.TerrainPreviewView;
import edu.playground.djivln.survey.TerrainAltitudeLegendView;
import edu.playground.djivln.survey.TerrainPreviewData;
import edu.playground.djivln.survey.TerrainPreviewSampler;
import edu.playground.djivln.survey.TerrainElevationSource;
import edu.playground.djivln.survey.TerrainRasterInfo;
import edu.playground.djivln.survey.SurveyConstraints;
import edu.playground.djivln.survey.SurveyCollectionMode;
import edu.playground.djivln.survey.SurveyCaptureView;
import edu.playground.djivln.survey.SurveyAltitudeMode;
import edu.playground.djivln.survey.SurveyStartPointMode;
import edu.playground.djivln.survey.SurveyCompletionAction;
import edu.playground.djivln.survey.SurveyCaptureTriggerMode;
import edu.playground.djivln.survey.SurveyTakeoffMode;
import edu.playground.djivln.survey.SurveyObliqueHeadingMode;
import edu.playground.djivln.survey.SurveyMission;
import edu.playground.djivln.survey.SurveyMissionKt;
import edu.playground.djivln.survey.SurveyPassWaypoints;
import edu.playground.djivln.survey.ActiveMappingPassMetadata;
import edu.playground.djivln.survey.SurveyNadirGimbalPolicy;
import edu.playground.djivln.survey.SurveyMissionCaptureViewFilter;
import edu.playground.djivln.survey.SurveyMissionJson;
import edu.playground.djivln.survey.SurveyMissionLibrary;
import edu.playground.djivln.survey.SurveyMissionVersion;
import edu.playground.djivln.survey.SurveyMissionReplay;
import edu.playground.djivln.survey.SurveyLowBatteryPolicy;
import edu.playground.djivln.survey.SurveyPlanner;
import edu.playground.djivln.survey.SurveyReplaySnapshot;
import edu.playground.djivln.survey.SurveyReplayState;
import edu.playground.djivln.survey.SurveyExecutionBlock;
import edu.playground.djivln.survey.SurveyExecutionGateResult;
import edu.playground.djivln.survey.SurveyExecutionEnvironment;
import edu.playground.djivln.survey.SurveyExecutionTelemetry;
import edu.playground.djivln.survey.SurveySimulatorGate;
import edu.playground.djivln.survey.SurveySimulatorExecutionStateMachine;
import edu.playground.djivln.survey.SurveyExecutionState;
import edu.playground.djivln.survey.SurveyExecutionPhase;
import edu.playground.djivln.survey.SurveyExecutionStatus;
import edu.playground.djivln.survey.SurveyRemainingEstimate;
import edu.playground.djivln.survey.SurveyFollowerCommand;
import edu.playground.djivln.survey.SurveyFollowerPose;
import edu.playground.djivln.survey.SurveyWaypointFollower;
import edu.playground.djivln.survey.SurveyExecutionWatchdog;
import edu.playground.djivln.survey.SurveyGimbalSettlePolicy;
import edu.playground.djivln.survey.SurveyFailsafeAction;
import edu.playground.djivln.survey.SurveyFailsafeDecision;
import edu.playground.djivln.survey.SurveyDistanceCaptureController;
import edu.playground.djivln.survey.SurveyExecutionCheckpoint;
import edu.playground.djivln.survey.SurveyExecutionCheckpointJson;
import edu.playground.djivln.survey.SurveyCheckpointRecoveryPolicy;
import edu.playground.djivln.survey.SurveyRecoveryPosition;
import edu.playground.djivln.survey.SurveyParameterPolicy;
import edu.playground.djivln.survey.SurveyUeBridgeClient;
import edu.playground.djivln.survey.SurveyUeBridgeContract;
import edu.playground.djivln.survey.SurveyUeCapture;
import edu.playground.djivln.survey.SurveyUeTarget;
import edu.playground.djivln.survey.SurveyUeTelemetry;
import edu.playground.djivln.survey.SurveyRealFlightBlock;
import edu.playground.djivln.survey.SurveyRealFlightEvidence;
import edu.playground.djivln.survey.SurveyRealFlightReadiness;
import edu.playground.djivln.survey.SurveyRealFlightReadinessReport;
import edu.playground.djivln.survey.SurveyRealFlightTelemetry;
import edu.playground.djivln.survey.SurveyRegressionMissionFactory;
import edu.playground.djivln.survey.SurveyRuntimeFaultPolicy;
import edu.playground.djivln.survey.SurveyWaypointDivergencePolicy;
import edu.playground.djivln.survey.SurveySimulatorSwitchAction;
import edu.playground.djivln.survey.SurveySimulatorSwitchPolicy;
import edu.playground.djivln.survey.ImportedMissionCameraCompatibility;
import edu.playground.djivln.survey.ImportedMissionCameraCompatibilityPolicy;
import edu.playground.djivln.survey.SurveyTerrainTakeoffReference;
import edu.playground.djivln.survey.SurveyTerrainTakeoffReferencePolicy;
import edu.playground.djivln.survey.SurveyTerrainTakeoffReferenceSource;
import edu.playground.djivln.survey.SurveyTerrainTakeoffVerification;
import edu.playground.djivln.survey.SurveySimulatorMapPose;
import edu.playground.djivln.survey.SurveySimulatorMapProjection;
import edu.playground.djivln.survey.TerrainImportSafety;
import edu.playground.djivln.survey.MapMarkerUpdatePolicy;
import edu.playground.djivln.survey.SurveyWaypoint;
import kotlin.Unit;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.useraccount.UserAccountState;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.media.MediaFile;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

public final class Mini2CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "DjiVln";
    private static final String MAP_PRIVACY_PREFS = "openfly_map_privacy";
    private static final String MAP_PRIVACY_ACCEPTED = "baidu_map_privacy_v1";
    private static final String BAIDU_MAP_PRIVACY_URL =
            "https://lbsyun.baidu.com/docs/pcsa?title=compliance/openprivacy";
    private static final int PERMISSION_REQUEST = 42;
    private static final int MODEL_PACK_REQUEST = 43;
    private static final int SURVEY_MISSION_IMPORT_REQUEST = 44;
    private static final int SURVEY_MISSION_EXPORT_REQUEST = 45;
    private static final int SURVEY_DSM_IMPORT_REQUEST = 46;
    private static final int SURVEY_BUILDING_HEIGHT_IMPORT_REQUEST = 47;
    private static final int V86_OFFLINE_FOLDER_REQUEST = 48;
    private static final int SURVEY_TAB_AREA = 0;
    private static final int SURVEY_TAB_ROUTE = 1;
    private static final int SURVEY_TAB_CAPTURE = 2;
    private static final int SURVEY_TAB_TERRAIN = 3;
    private static final String ACTION_MODEL_HEALTH = "com.openfly.go.v4.action.MODEL_HEALTH";
    private static final String ACTION_MODEL_SMOKE = "com.openfly.go.v4.action.MODEL_SMOKE";
    private static final String ACTION_MOCK_UI_STATE = "com.openfly.go.v4.action.MOCK_UI_STATE";
    private static final String ACTION_SURVEY_REGRESSION =
            "com.openfly.go.v4.action.SURVEY_REGRESSION";
    private static final String ACTION_SURVEY_UI_DRY_RUN_CONTROL =
            "com.openfly.go.v4.action.SURVEY_UI_DRY_RUN_CONTROL";
    private static final String ACTION_HIL_SIMULATOR_REGRESSION =
            "com.openfly.go.v4.action.HIL_SIMULATOR_REGRESSION";
    private static final String ACTION_HIL_RECONNECT_SIMULATOR_REGRESSION =
            "com.openfly.go.v4.action.HIL_RECONNECT_SIMULATOR_REGRESSION";
    private static final String ACTION_HIL_OFFLINE_REGRESSION =
            "com.openfly.go.v4.action.HIL_OFFLINE_REGRESSION";
    private static final String ACTION_HIL_RESET_AND_TAKEOFF =
            "com.openfly.go.v4.action.HIL_RESET_AND_TAKEOFF";
    private static final String ACTION_HIL_AXIS_CALIBRATION =
            "com.openfly.go.v4.action.HIL_AXIS_CALIBRATION";
    private static final String ACTION_CAMERA_CADENCE_TEST =
            "com.openfly.go.v4.action.CAMERA_CADENCE_TEST";
    private static final long[] CAMERA_CADENCE_TEST_PERIODS_MS =
            new long[] {2_000L, 1_600L, 1_400L, 1_200L, 1_000L};
    private static final String SURVEY_SESSION_PREFERENCES = "survey-session";
    private static final String SURVEY_MISSION_KEY = "mission-json";
    private static final String SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY =
            "active-recapture-source-mission-json";
    private static final String SURVEY_LIBRARY_KEY = "mission-library-json";
    private static final String SURVEY_CHECKPOINT_KEY = "checkpoint-json";
    private static final String SURVEY_SETTINGS_KEY = "planner-settings-json";
    private static final String SURVEY_LAST_JSON_DOCUMENT_URI_KEY = "last-json-document-uri";
    private static final int SURVEY_SETTINGS_SCHEMA_VERSION = 1;
    /** DJI MSDK4 documents 5-25 Hz; use the documented maximum for survey VS. */
    private static final long SURVEY_CONTROL_INTERVAL_MS = 40L;
    private static final long VLN_CONTROL_INTERVAL_MS = 40L;
    private static final double VLN_CONTROL_INTERVAL_SECONDS = 0.04;
    private static final int MODEL_FRAME_WIDTH = 1440;
    private static final int MODEL_FRAME_HEIGHT = 1080;
    private static final String[] PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService modelExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService storageExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean activityDestroyed;
    private final String storageSessionId = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    private volatile Uri sessionLogUri;
    private volatile File legacySessionLogFile;
    private boolean storageInitQueued;
    private TextView statusText;
    private TextView modelStatusText;
    private TextView modelResultText;
    private TextView safetyGateText;
    private TextView flightStatusText;
    private TextView flightModeText;
    private TextView gpsStatusText;
    private TextView rcStatusText;
    private SignalQualityView rcSignalView;
    private TextView aircraftBatteryText;
    private RemoteControllerBatteryView rcBatteryText;
    private TextView compassText;
    private TextView speedText;
    private TextView altitudeText;
    private TextView distanceText;
    private TextView locationSourceText;
    private TextView cameraModeText;
    private TextView recordingTimeText;
    private TextView commandText;
    private TextView advancedStateText;
    private TextView logText;
    private TextView toastBanner;
    private TextView surveyPhotoCaptureFeedback;
    private EditText modelPrompt;
    private EditText ethernetEndpoint;
    private Button localTransportButton;
    private Button usbTransportButton;
    private Button ethernetTransportButton;
    private Button cloudModelButton;
    private SurfaceTexture videoSurfaceTexture;
    private int surfaceWidth;
    private int surfaceHeight;
    private DJICodecManager codecManager;
    private VideoFeeder.VideoFeed videoFeed;
    private String videoFeedName = "none";
    private long packetCount;
    private long byteCount;
    private final AtomicLong decodedFrameSequence = new AtomicLong();
    private final AtomicLong lastDecodedFrameRenderedElapsedNanos = new AtomicLong();
    private String sdkState = "not registered";
    private DjiUserAccountController djiAccountController;
    private V86StreamingController v86Controller;
    private String lastV86StatusLog = "";
    private int lastV86DownloadPercent = -1;
    private TextView djiAccountStatusText;
    private Button djiAccountLoginButton;
    private String lastDjiAccountLogState = "";
    private ModelTransport modelTransport = ModelTransport.ETHERNET;
    private Mini2OpenFlyRuntime localOpenFlyRuntime;
    private AoaUsbProbe aoaUsbProbe;
    private Mini2AircraftBridge aircraftBridge;
    private volatile Mini2AircraftBridge.Snapshot aircraftSnapshot = new Mini2AircraftBridge.Snapshot();
    private boolean controlArmed;
    private boolean emergencyStopped;
    private boolean autoInferenceEnabled;
    private boolean continuousChunkEnabled;
    private int executedPrefix = UAVFlowPolicyContract.DEFAULT_EXECUTED_PREFIX;
    private boolean flyThroughEnabled = true;
    private boolean chunkExecutionActive;
    private int chunkStepsExecuted;
    private int chunkRemaining;
    private boolean velocityEstimateMode;
    private boolean monitorFollow = true;
    private boolean logAutoScrollInProgress;
    private boolean logScrollScheduled;
    private boolean mapFullscreen;
    private boolean mapSatellite;
    private boolean mapThreeDimensional;
    private float speedLimitMetersPerSecond = 0.5f;
    private long lastFrameAtElapsedMs;
    private long lastTelemetryAtElapsedMs;
    private boolean inferenceInFlight;
    private boolean modelLoaded;
    private double stopThreshold = UAVFlowPolicyContract.DEFAULT_STOP_THRESHOLD;
    private int modelLoadPollAttempts;
    private Runnable activeRelativeMoveRunnable;
    private Long latestInferenceLatencyMs;
    private long inferenceStartedAtElapsedMs;
    private double lastYawRateDegreesPerSecond;
    private double chunkPlannedHeadingDegrees = Double.NaN;
    private double flyThroughCarryNorthMeters;
    private double flyThroughCarryEastMeters;
    private double flyThroughCarryUpMeters;
    private final ArrayDeque<OpenFlyRelativeAction> pendingPolicyActions = new ArrayDeque<>();
    private int lastInferenceSourceWidth = -1;
    private int lastInferenceSourceHeight = -1;
    private boolean vlnPanelMinimized;
    private boolean flightPanelMinimized;
    private int lastResponsiveHudWidth;
    private final Map<View, Integer> modalHiddenVisibilities = new HashMap<>();
    private final List<MediaFile> mediaFiles = new ArrayList<>();
    private final List<MediaFile> mediaShown = new ArrayList<>();
    private final Map<String, Bitmap> mediaThumbs = new HashMap<>();
    private final Set<String> mediaThumbPending = new HashSet<>();
    private String mediaFilter = "all";
    private BaseAdapter mediaAdapter;
    private String settingsTab = "link";
    private boolean mockUiActive;
    private boolean mockCameraUnavailable;
    private boolean mainUiInitialized;
    private boolean cloudModelDownloadInFlight;
    private volatile boolean modelMaintenanceInFlight;
    private String lastCloudProgressPhase = "";
    private int lastCloudProgressBucket = -1;
    private final StringBuilder runtimeLog = new StringBuilder();
    private int runtimeLogLines;
    private String lastSdkLogSummary = "";
    private long lastSdkLogAtElapsedMs;
    private long lastMemoryLogElapsedMs;
    private MapView mapView;
    private BaiduMap amap;
    private Marker aircraftMarker;
    private Marker remoteControllerMarker;
    private GeoPoint lastAircraftMarkerPoint;
    private Double lastAircraftMarkerHeading;
    private long lastAircraftMarkerUpdateMs;
    private boolean aircraftMarkerVisible;
    private GeoPoint lastRemoteControllerMarkerPoint;
    private Double lastRemoteControllerMarkerHeading;
    private long lastRemoteControllerMarkerUpdateMs;
    private boolean remoteControllerMarkerVisible;
    private String lastRemoteControllerHeadingSource = "NONE";
    private String lastRemoteControllerLocationSource = "NONE";
    private PhoneHeadingSource phoneHeadingSource;
    private volatile PhoneHeadingSource.Heading phoneHeading;
    private Marker homeMarker;
    private Polyline homeDirectionLine;
    private GeoPoint lastHomeMarkerPoint;
    private GeoPoint lastHomeLineAircraftPoint;
    private GeoPoint lastHomeLinePoint;
    private BitmapDescriptor aircraftMapIcon;
    private BitmapDescriptor remoteControllerArrowMapIcon;
    private BitmapDescriptor remoteControllerStaticMapIcon;
    private BitmapDescriptor homeMapIcon;
    private boolean mapBaseLoaded;
    private String lastMapFocusSource = "NONE";
    private Marker surveyReplayMarker;
    private Marker surveyStartMarker;
    private Marker surveyExecutionTargetMarker;
    private Polygon surveyCoveragePolygon;
    private Polygon surveyTargetPolygon;
    private Polygon surveyPolygon;
    private final List<Polyline> surveyRoutes = new ArrayList<>();
    private Polyline surveyExecutionRoute;
    private final List<Marker> surveyVertexMarkers = new ArrayList<>();
    private final List<Marker> surveyActiveCaptureMarkers = new ArrayList<>();
    private int selectedSurveyVertexIndex = -1;
    private final Set<SurveyCaptureView> surveyEnabledCaptureViews =
            EnumSet.of(SurveyCaptureView.NADIR, SurveyCaptureView.FORWARD_OBLIQUE,
                    SurveyCaptureView.BACKWARD_OBLIQUE, SurveyCaptureView.LEFT_OBLIQUE,
                    SurveyCaptureView.RIGHT_OBLIQUE);
    private boolean surveySpeedHintFiveDirection;
    private final List<GeoPoint> surveyRoi = new ArrayList<>();
    private SurveyMission surveyMission;
    private SurveyMission surveyPlannedEtaSource;
    private SurveyRemainingEstimate surveyPlannedEta;
    private SurveyMission surveyCaptureSelectionSourceMission;
    private SurveyMission activeRecaptureSourceMission;
    private TerrainElevationSource surveyTerrain;
    private TerrainElevationSource surveyGlobalTerrainBase;
    private String surveyTerrainSha256;
    private double[] surveyTerrainPreviewGrid;
    private TerrainPreviewData surveyTerrainPreviewData;
    private boolean surveyTerrainCalculationInFlight;
    private boolean surveyGsdLinkUpdating;
    private boolean surveySettingsRestoring;
    private final Runnable persistSurveySettingsRunnable = this::persistSurveyPlannerSettings;
    private SurveyMissionReplay surveyReplay;
    private SurveySimulatorExecutionStateMachine surveySimulatorExecution;
    private SurveyExecutionEnvironment surveyExecutionEnvironment;
    private long surveyWaypointDeadlineElapsedMs;
    private long surveyLegStartedElapsedMs;
    private double surveyBestWaypointHorizontalErrorMeters = Double.POSITIVE_INFINITY;
    private long surveyLastWaypointProgressElapsedMs;
    private long surveyControlStartedElapsedMs;
    private long surveyNextControlTickUptimeMs;
    private long surveyControlTickCount;
    private long surveyFreshFlightStateCount;
    private long surveyLastFlightStateUpdatedAtMs;
    private long lastSurveyExecutionRenderElapsedMs;
    private long lastSurveyFlightLogElapsedMs;
    private int renderedSurveyExecutionLegIndex = -1;
    private final SurveyDistanceCaptureController surveyCaptureController =
            new SurveyDistanceCaptureController();
    private double surveyGimbalCommandedPitch = Double.NaN;
    private long surveyGimbalSettlingStartedElapsedMs;
    private long surveyGimbalLastCommandElapsedMs;
    private long surveyGimbalCommandAcceptedElapsedMs;
    private long surveyGimbalVerificationStartedElapsedMs;
    private long surveyGimbalCommandGeneration;
    private int surveyGimbalCommandAttempts;
    private boolean surveyGimbalLimitActive;
    private SurveyWaypoint surveyPendingCaptureStart;
    private boolean surveyPhotoInFlight;
    private long surveyPhotoRequestGeneration;
    private long surveyTriggerFrameId;
    private long triggerFrameSequence;
    private final Map<Long, PendingTriggerFrame> pendingTriggerFrames = new HashMap<>();
    private int surveyPointCapturePendingLegIndex = -1;
    private int surveyPointCaptureCompletedLegIndex = -1;

    private static final class PendingTriggerFrame {
        final long id;
        TriggerFrameMetadata metadata;
        final long baselineVideoSequence;
        final long triggeredAtElapsedNanos;
        Bitmap bitmap;
        boolean bitmapRequested;
        Boolean photoSucceeded;
        String resultMessage;

        PendingTriggerFrame(long id, TriggerFrameMetadata metadata,
                            long baselineVideoSequence, long triggeredAtElapsedNanos) {
            this.id = id;
            this.metadata = metadata;
            this.baselineVideoSequence = baselineVideoSequence;
            this.triggeredAtElapsedNanos = triggeredAtElapsedNanos;
        }
    }
    private long lastSurveyUeCapturedReceivedNanos;
    private boolean cameraCadenceTestActive;
    private int cameraCadenceTestGeneration;
    private long[] cameraCadenceTestPeriodsMs = CAMERA_CADENCE_TEST_PERIODS_MS.clone();
    private int cameraCadenceStageIndex;
    private int cameraCadenceStageShotIndex;
    private int cameraCadenceShotsPerStage;
    private int cameraCadenceStageOk;
    private int cameraCadenceStageFail;
    private int cameraCadenceStageTimeout;
    private long cameraCadenceStageLatencyMs;
    private long cameraCadenceStageFirstRequestUptimeMs;
    private long cameraCadenceStageLastRequestUptimeMs;
    private long cameraCadenceNextRequestUptimeMs;
    private boolean cameraCadenceShotInFlight;
    private Runnable cameraCadenceShotTimeoutRunnable;
    private final Runnable cameraCadenceTickRunnable = this::runCameraCadenceTestTick;
    private AlertDialog surveyLowBatteryDialog;
    private long surveyLowBatteryReturnDeadlineElapsedMs;
    private boolean surveyLowBatteryReturnIssued;
    private boolean surveyLowBatteryGoHomeCommandSent;
    private final Runnable surveyLowBatteryReturnRunnable = new Runnable() {
        @Override public void run() {
            if (surveyLowBatteryReturnDeadlineElapsedMs == 0L || surveyLowBatteryReturnIssued) return;
            if (!shouldForceSurveyLowBatteryReturn()) {
                cancelSurveyLowBatteryReturn(getString(R.string.reason_trigger_cleared));
                return;
            }
            long remainingMs = surveyLowBatteryReturnDeadlineElapsedMs - SystemClock.elapsedRealtime();
            if (remainingMs <= 0L) {
                issueSurveyLowBatteryReturn();
                return;
            }
            updateSurveyLowBatteryDialog((int) Math.ceil(remainingMs / 1_000.0));
            mainHandler.postDelayed(this, Math.min(1_000L, remainingMs));
        }
    };
    private boolean surveyAutoTakeoffPending;
    private long surveyAutoTakeoffDeadlineElapsedMs;
    private long surveyAutoTakeoffFlyingSinceElapsedMs;
    private final Runnable surveyAutoTakeoffRunnable = new Runnable() {
        @Override public void run() {
            if (!surveyAutoTakeoffPending || surveyMission == null) return;
            long now = SystemClock.elapsedRealtime();
            if (!aircraftSnapshot.getConnected() || !aircraftSnapshot.getSimulatorActive()
                    || aircraftSnapshot.getSticksActive()) {
                cancelSurveyAutoTakeoff(getString(R.string.reason_auto_takeoff_safety_lost));
                return;
            }
            if (now >= surveyAutoTakeoffDeadlineElapsedMs) {
                cancelSurveyAutoTakeoff(getString(R.string.reason_auto_takeoff_unconfirmed_timeout));
                return;
            }
            boolean stableHover = aircraftSnapshot.getSimulatorFlying()
                    && aircraftSnapshot.getAltitude() >= 0.8
                    && Math.abs(aircraftSnapshot.getVerticalSpeed()) <= 0.5;
            if (stableHover) {
                if (surveyAutoTakeoffFlyingSinceElapsedMs == 0L) {
                    surveyAutoTakeoffFlyingSinceElapsedMs = now;
                }
                if (now - surveyAutoTakeoffFlyingSinceElapsedMs >= 1_000L) {
                    surveyAutoTakeoffPending = false;
                    mainHandler.removeCallbacks(this);
                    appendLog("SURVEY auto takeoff stable · continuing to route arming");
                    startSurveySimulatorExecution();
                    return;
                }
            } else {
                surveyAutoTakeoffFlyingSinceElapsedMs = 0L;
            }
            mainHandler.postDelayed(this, 200L);
        }
    };
    private final Runnable surveyPhotoTimeoutRunnable = () -> {
        if (!surveyPhotoInFlight) return;
        invalidateSurveyPhotoRequest();
        appendLog("SURVEY photo timeout · pausing mission");
        pauseSurveyForRecoverableFault(getString(R.string.reason_capture_callback_timeout), true);
    };
    private final SurveyUeBridgeClient ueBridgeClient = new SurveyUeBridgeClient();
    private boolean ueBridgeEnabled;
    private long lastUeBridgePostElapsedMs;
    private AndroidHilController hilController;
    private HilConnectionMode hilConnectionMode = HilConnectionMode.HOTSPOT;
    private boolean hilVirtualFramesEnabled;
    private long hilVirtualFramesEnabledAtElapsedMs;
    private long hilVirtualFrameUnavailableSinceElapsedMs;
    private boolean hilVirtualFrameEverReady;
    private boolean hilVirtualFrameWaitLogged;
    private boolean hilMovingRawStaleLatched;
    private boolean hilAppInBackground;
    private boolean hilSimulatorAutoStartPending;
    private boolean hilSimulatorStartInFlight;
    private boolean hilStartedSimulator;
    // An already-active grounded Simulator owns a reusable authoritative RAW sample.
    // Do not stop/start it merely because this App process or HIL session is new.
    private boolean hilSimulatorCleanStartRequired;
    private int hilSimulatorStartGeneration;
    private int hilSimulatorStartAttempts;
    private long hilSimulatorNavigationUnreadySinceMs;
    private boolean hilSimulatorGroundRecoveryInFlight;
    private boolean hilSimulatorCommandPoisoned;
    private String hilSimulatorCommandPoisonReason = "";
    private int hilSimulatorStateHz = 100;
    private AndroidHilController.Status latestHilStatus;
    private HilOfflineRegressionRunner hilOfflineRegressionRunner;
    private boolean hilSimulatorRegressionActive;
    private int hilSimulatorRegressionStage;
    private long hilSimulatorRegressionStartedMs;
    private long hilSimulatorRegressionStageStartedMs;
    private long hilSimulatorRegressionLastWaitLogMs;
    private int hilSimulatorRegressionGeneration;
    private Mini2AircraftBridge.SimulatorSample hilSimulatorRegressionOrigin;
    private Mini2AircraftBridge.SimulatorSample hilSimulatorRegressionBaseline;
    private String hilSimulatorRegressionTakeoffResult = "not-run";
    private boolean hilSimulatorRegressionSawMotors;
    private boolean hilSimulatorRegressionSawFlying;
    private double hilSimulatorRegressionMaxRawHz;
    private double hilSimulatorRegressionMaxAltitudeGain;
    private double hilSimulatorRegressionMaxForwardMovement;
    private double hilSimulatorRegressionMaxForwardAttitude;
    private int hilSimulatorRegressionTakeoffAttempts;
    private boolean hilSimulatorRegressionMotorFallback;
    private long hilSimulatorRegressionNavigationReadySinceMs;
    private boolean hilSimulatorRegressionLandingConfirmationRequested;
    private long hilSimulatorRegressionLastLandingLogMs;
    private int hilSimulatorRegressionCompletedCycles;
    private static final int HIL_SIMULATOR_REGRESSION_TARGET_CYCLES = 2;
    private boolean hilUiSimulatorTakeoffPending;
    private boolean hilUiSimulatorTakeoffAccepted;
    private boolean hilUiSimulatorPostAcceptRefreshAttempted;
    private long hilUiSimulatorAirborneDeadlineMs;
    private int hilUiSimulatorTakeoffGeneration;
    private long hilUiSimulatorTakeoffStartedMs;
    private long hilUiSimulatorTakeoffReadySinceMs;
    private int hilUiSimulatorTakeoffAttempts;
    private boolean hilUiSimulatorRecoveryInFlight;
    private boolean hilUiSimulatorRecoveryAttempted;
    private boolean hilUiSimulatorProductReconnectAttempted;
    private boolean hilUiSimulatorMotorFallbackActive;
    private long hilUiSimulatorMotorFallbackDeadlineMs;
    private boolean hilAxisCalibrationActive;
    private int hilAxisCalibrationGeneration;
    private HilLoopbackPeer hilSimulatorRegressionLoopbackPeer;
    private boolean hilSimulatorRegressionOwnsLink;
    private boolean hilSimulatorRegressionCleanupActive;
    private final Runnable hilSimulatorRegressionRunnable = this::runHilSimulatorRegressionTick;
    private boolean surveyPlanningActive;
    private int surveyPlannerTab = SURVEY_TAB_AREA;
    private boolean surveyTerrainFollowingEnabled;
    private boolean mapInitiallyFramed;
    private Location lastPhoneMapLocation;
    private long lastThumbnailMapFollowElapsedMs;
    private boolean phoneMapLocationRequested;
    private boolean phoneMapLocationPending;
    private boolean phoneMapLocationResolved;
    private final Runnable phoneMapLocationTimeoutRunnable = () -> {
        if (!phoneMapLocationPending || phoneMapLocationResolved) return;
        phoneMapLocationPending = false;
        phoneMapLocationResolved = true;
        appendLog("MAP phone GPS timeout; using aircraft when available");
        updateMap(aircraftSnapshot);
    };
    private static final int EXPECTED_PROMPT_PRESET_COUNT = 5;
    private static final long HIL_ASYNC_FRAME_FRESH_MILLIS = 2_500L;
    private static final long HIL_SURVEY_FRAME_WAIT_MILLIS = 7_000L;
    private static final long HIL_SURVEY_FRAME_POLL_MILLIS = 80L;
    private static final long HIL_ASYNC_FRAME_STARTUP_GRACE_MILLIS = 15_000L;
    private static final long HIL_ASYNC_FRAME_DROPOUT_GRACE_MILLIS = 5_000L;
    private static final long PHONE_MAP_LOCATION_TIMEOUT_MS = 5_000L;
    private static final long PHONE_MAP_LAST_KNOWN_MAX_AGE_MS = 5 * 60_000L;
    private static final float PHONE_MAP_LAST_KNOWN_MAX_ACCURACY_METERS = 1_000.0f;
    private final List<String[]> promptPresets = new ArrayList<>();

    private final Runnable surveyReplayRunnable = new Runnable() {
        @Override public void run() {
            if (surveyReplay == null || surveyReplay.getState() != SurveyReplayState.RUNNING) return;
            SurveyReplaySnapshot snapshot = surveyReplay.advance();
            renderSurveyReplay(snapshot);
            if (snapshot.getState() == SurveyReplayState.RUNNING) {
                mainHandler.postDelayed(this, 80L);
            }
        }
    };

    private final Runnable surveySimulatorControlRunnable = new Runnable() {
        @Override public void run() {
            runSurveySimulatorControlTick();
        }
    };

    private boolean surveyUiDryRunMode;
    /** Debug-only planning origin used to preview ASL-to-AGL conversion without an aircraft. */
    private GeoPoint surveyDebugPreviewTakeoffPoint;
    private final Runnable surveyUiDryRunRunnable = new Runnable() {
        @Override public void run() {
            if (!surveyUiDryRunMode || surveySimulatorExecution == null) return;
            SurveyExecutionStatus status = surveySimulatorExecution.getStatus();
            if (status.getState() != SurveyExecutionState.RUNNING) return;
            status = surveySimulatorExecution.reachWaypoint();
            renderSurveyExecutionOverlay(true);
            renderSurveySimulatorExecutionStatus(status, null);
            if (status.getState() == SurveyExecutionState.COMPLETED) {
                surveyUiDryRunMode = false;
                clearPersistedSurveyCheckpoint();
                appendLog("SURVEY UI DRY RUN COMPLETED · NO_CONTROL · NO_CAMERA");
                renderSurveyStatus(getString(R.string.survey_ui_simulation_completed));
                return;
            }
            appendLog("SURVEY UI DRY RUN leg="
                    + (surveySimulatorExecution.getExecutionLegIndex() + 1) + "/"
                    + surveySimulatorExecution.getExecutionLegCount() + " · NO_CONTROL");
            mainHandler.postDelayed(this, 450L);
        }
    };

    private final Runnable autoInferenceRunnable = new Runnable() {
        @Override public void run() {
            if (!autoInferenceEnabled) return;
            if (continuousChunkEnabled) {
                // In combined mode, start a new plan only after the previous full
                // chunk has completed. Never overwrite a position action mid-flight.
                if (controlArmed && !chunkExecutionActive && activeRelativeMoveRunnable == null
                        && !inferenceInFlight) {
                    inferCurrentFrame();
                }
            } else if (!inferenceInFlight) {
                inferCurrentFrame();
            }
            mainHandler.postDelayed(this, continuousChunkEnabled ? 650L : 1_000L);
        }
    };

    private final Runnable modelLoadPollRunnable = () -> {
        if (modelLoaded || modelLoadPollAttempts >= 240) return;
        modelLoadPollAttempts++;
        runModelOperation("health");
    };

    private final Runnable stopVelocityRunnable = () ->
            finishRelativeMove(true, getString(R.string.reason_position_action_complete));

    private boolean hasFollowingContinuousStep() {
        return chunkExecutionActive && continuousChunkEnabled && controlArmed
                && chunkRemaining > 0 && !pendingPolicyActions.isEmpty()
                && chunkStepsExecuted < executedPrefix;
    }

    private void finishRelativeMove(boolean successful, String reason) {
        if (activeRelativeMoveRunnable != null) {
            mainHandler.removeCallbacks(activeRelativeMoveRunnable);
            activeRelativeMoveRunnable = null;
        }
        boolean continueChunk = successful && chunkExecutionActive && continuousChunkEnabled
                && controlArmed && chunkRemaining > 0 && !pendingPolicyActions.isEmpty()
                && chunkStepsExecuted < executedPrefix;
        if (continueChunk) {
            if (!flyThroughEnabled && aircraftBridge != null) {
                aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
                lastYawRateDegreesPerSecond = 0.0;
            }
            appendLog(getString(R.string.uavflow_step_completed_queue,
                    executedPrefix, chunkStepsExecuted, executedPrefix, chunkRemaining,
                    getString(flyThroughEnabled
                            ? R.string.uavflow_flythrough_no_zero
                            : R.string.uavflow_stop_each_waypoint)));
            captureIntermediateObservationThen(this::executeNextQueuedPolicyAction);
            return;
        }
        if (aircraftBridge != null) aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
        lastYawRateDegreesPerSecond = 0.0;
        resetChunkTrajectoryReference();
        commandText.setText((latestInferenceLatencyMs == null ? "IDLE" : latestInferenceLatencyMs + "ms")
                + "    X 0.0  Y 0.0  Z 0.0  YAW 0.0");
        appendLog("CONTROL zero velocity / hold");
        if (chunkExecutionActive) {
            if (successful) {
                appendLog(getString(R.string.uavflow_chunk_completed,
                        executedPrefix, chunkStepsExecuted));
                chunkExecutionActive = false;
                chunkRemaining = 0;
                pendingPolicyActions.clear();
            } else {
                abortChunkExecution(reason);
            }
        }
    }

    private final VideoFeeder.VideoDataListener videoListener = (videoBuffer, size) -> {
        packetCount++;
        byteCount += size;
        lastFrameAtElapsedMs = SystemClock.elapsedRealtime();
        DJICodecManager codec = codecManager;
        if (codec != null) {
            codec.sendDataToDecoder(videoBuffer, size);
        }
    };

    private final Runnable statusUpdater = new Runnable() {
        @Override public void run() {
            attachVideoFeed();
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            String productName = product == null ? "none" : "DJI Aircraft";
            String connected = product != null && product.isConnected() ? "yes" : "no";
            String feed = videoFeed == null ? "none" : videoFeedName;
            String text = String.format(Locale.US,
                    "MSDK %s | %s\nProduct: %s | connected: %s\nVideo feed: %s | H264 packets: %d | %.1f MB\n%s",
                    DJISDKManager.getInstance().getSDKVersion(), sdkState,
                    productName, connected, feed, packetCount, byteCount / 1048576.0,
                    packetCount == 0 ? "Waiting for DJI camera stream..." : "Camera stream received / decoding");
            statusText.setText(text);
            String logSummary = text.replace('\n', ' ');
            long logNow = SystemClock.elapsedRealtime();
            if (!logSummary.equals(lastSdkLogSummary) && logNow - lastSdkLogAtElapsedMs >= 10_000L) {
                Log.i(TAG, logSummary);
                lastSdkLogSummary = logSummary;
                lastSdkLogAtElapsedMs = logNow;
            }
            if (mockUiActive) {
                long now = SystemClock.elapsedRealtime();
                if (!mockCameraUnavailable) lastFrameAtElapsedMs = now;
                lastTelemetryAtElapsedMs = now;
            }
            refreshCameraPictureInPictureAvailability();
            publishUeTelemetryIfEnabled(aircraftSnapshot);
            renderSafetyState();
            mainHandler.postDelayed(this, 1000);
        }
    };

    private final DJISDKManager.SDKManagerCallback sdkCallback = new DJISDKManager.SDKManagerCallback() {
        @Override public void onRegister(DJIError error) {
            sdkState = error == DJISDKError.REGISTRATION_SUCCESS
                    ? "registered" : "register failed: " + (error == null ? "unknown" : error.getDescription());
            Log.i(TAG, sdkState);
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                mainHandler.post(() -> {
                    if (djiAccountController != null) djiAccountController.refresh();
                });
            }
        }

        @Override public void onProductDisconnect() {
            Log.i(TAG, "product disconnected");
            detachVideoFeed();
            mainHandler.post(() -> {
                invalidateHilSimulatorActivation(getString(R.string.reason_dji_product_temporarily_disconnected));
                pauseHilForTransientSourceLoss(getString(R.string.reason_dji_product_temporarily_disconnected));
            });
            if (aircraftBridge != null) aircraftBridge.invalidateCurrentProduct();
        }

        @Override public void onProductConnect(BaseProduct product) {
            Log.i(TAG, "product connected: " + product);
            if (aircraftBridge != null) aircraftBridge.bindCurrentProduct();
            mainHandler.postDelayed(() -> resetVideoFeed(), 500L);
            mainHandler.postDelayed(() -> {
                if (packetCount == 0) resetVideoFeed();
            }, 2_000);
        }

        @Override public void onProductChanged(BaseProduct product) {
            mainHandler.post(() -> invalidateHilSimulatorActivation(
                    getString(R.string.reason_dji_product_session_changed)));
            if (aircraftBridge != null) {
                aircraftBridge.invalidateCurrentProduct();
                aircraftBridge.bindCurrentProduct();
            }
            mainHandler.postDelayed(() -> resetVideoFeed(), 500L);
        }

        @Override public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
            if (aircraftBridge != null) aircraftBridge.bindCurrentProduct();
            if (newComponent != null) {
                newComponent.setComponentListener(isConnected -> mainHandler.post(() -> {
                    if (aircraftBridge != null) aircraftBridge.bindCurrentProduct();
                    attachVideoFeed();
                }));
            }
        }

        @Override public void onInitProcess(DJISDKInitEvent event, int totalProcess) {
            Log.d(TAG, "init " + event + " " + totalProcess);
        }

        @Override public void onDatabaseDownloadProgress(long current, long total) {}
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allowContentAcrossDisplayCutout();
        enterImmersiveMode();
        if (!getSharedPreferences(MAP_PRIVACY_PREFS, MODE_PRIVATE)
                .getBoolean(MAP_PRIVACY_ACCEPTED, false)) {
            requestBaiduMapPrivacyConsent(savedInstanceState);
            return;
        }
        continueCreateAfterMapPrivacy(savedInstanceState);
    }

    private void requestBaiduMapPrivacyConsent(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.map_privacy_title)
                .setMessage(R.string.map_privacy_message)
                .setPositiveButton(R.string.action_agree_enable, null)
                .setNegativeButton(R.string.action_exit_app, null)
                .setNeutralButton(R.string.action_view_policy, null)
                .setCancelable(false)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                getSharedPreferences(MAP_PRIVACY_PREFS, MODE_PRIVATE).edit()
                        .putBoolean(MAP_PRIVACY_ACCEPTED, true).apply();
                dialog.dismiss();
                continueCreateAfterMapPrivacy(savedInstanceState);
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> finish());
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BAIDU_MAP_PRIVACY_URL))));
        });
        dialog.show();
    }

    private void continueCreateAfterMapPrivacy(Bundle savedInstanceState) {
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.GCJ02);
        setContentView(R.layout.activity_mini2_camera);
        Mini2Application application = (Mini2Application) getApplication();
        vlnPanelMinimized = application.isVlnPanelMinimized();
        flightPanelMinimized = application.isFlightPanelMinimized();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) initPublicCaptureStorage();
        statusText = findViewById(R.id.status_text);
        initModelPanel();
        initFullFlightUi(savedInstanceState);
        v86Controller = new V86StreamingController(this, snapshot -> {
            String summary = "session=" + snapshot.getSessionId()
                    + " captured=" + snapshot.getCapturedCount()
                    + " uploaded=" + snapshot.getUploadedCount()
                    + " pending=" + snapshot.getPendingCount()
                    + " rejected=" + snapshot.getRejectedCount()
                    + " message=" + snapshot.getMessage()
                    + (snapshot.getError() == null ? "" : " error=" + snapshot.getError());
            if (!summary.equals(lastV86StatusLog)) {
                lastV86StatusLog = summary;
                appendLog("V86 " + summary);
            }
            renderV86Status(snapshot);
        });
        phoneHeadingSource = new PhoneHeadingSource(this, heading -> {
            phoneHeading = heading;
            runOnUiThread(() -> updateRemoteControllerMapMarker(aircraftSnapshot));
        });
        phoneHeadingSource.start();
        initAircraftBridge();
        restoreSurveySession();
        applyPanelMinimizedState();
        View rootHud = findViewById(R.id.root_hud);
        rootHud.addOnLayoutChangeListener((view, left, top, right, bottom,
                                           oldLeft, oldTop, oldRight, oldBottom) -> {
            int width = right - left;
            if (width > 0 && width != lastResponsiveHudWidth) {
                lastResponsiveHudWidth = width;
                applyResponsiveHudLayout();
            }
        });
        rootHud.post(this::applyResponsiveHudLayout);
        TextureView textureView = findViewById(R.id.video_surface);
        textureView.setSurfaceTextureListener(this);
        if (textureView.isAvailable()) {
            videoSurfaceTexture = textureView.getSurfaceTexture();
            surfaceWidth = textureView.getWidth();
            surfaceHeight = textureView.getHeight();
            rebuildCodec();
        }
        textureView.setOnTouchListener((view, event) -> {
            if (!mapFullscreen || !hasUsableCameraPictureInPicture()) return false;
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                view.performClick();
                showSurveyPlanner(false);
            }
            // The camera PIP is above the map in picture-in-picture mode. Always consume
            // the gesture so it can never fall through and create a survey vertex.
            return true;
        });
        View mockVideo = findViewById(R.id.mock_video_background);
        if (mockVideo != null) mockVideo.setOnClickListener(v -> {
            if (mapFullscreen && hasUsableCameraPictureInPicture()) showSurveyPlanner(false);
        });
        mainUiInitialized = true;
        handleDebugControlIntent(getIntent());
        requestPermissionsAndRegister();
        mainHandler.post(statusUpdater);
        rootHud.post(() -> AppLanguageController.maybeShowFirstLaunch(this));
    }

    private void initModelPanel() {
        modelStatusText = findViewById(R.id.model_status);
        modelResultText = findViewById(R.id.model_result);
        modelPrompt = findViewById(R.id.model_prompt);
        ethernetEndpoint = findViewById(R.id.ethernet_endpoint);
        localTransportButton = findViewById(R.id.transport_local);
        usbTransportButton = findViewById(R.id.transport_usb);
        ethernetTransportButton = findViewById(R.id.transport_ethernet);
        cloudModelButton = findViewById(R.id.download_cloud_model);
        android.content.SharedPreferences vlnPreferences = getSharedPreferences("vln", MODE_PRIVATE);
        stopThreshold = Math.max(0.1, Math.min(0.9,
                vlnPreferences.getFloat("uavflow-stop-threshold", 0.7f)));
        Button stopThresholdButton = findViewById(R.id.denoise_steps_button);
        renderStopThresholdButton(stopThresholdButton);

        ethernetEndpoint.setText(ModelEndpoint.DEFAULT_ETHERNET_BASE);
        localTransportButton.setVisibility(View.GONE);
        usbTransportButton.setVisibility(View.GONE);
        ethernetTransportButton.setVisibility(View.GONE);
        findViewById(R.id.import_model).setVisibility(View.GONE);
        cloudModelButton.setVisibility(View.GONE);
        stopThresholdButton.setVisibility(View.GONE);
        findViewById(R.id.model_health).setVisibility(View.GONE);
        findViewById(R.id.model_load).setVisibility(View.GONE);
        findViewById(R.id.model_preflight).setVisibility(View.GONE);
        findViewById(R.id.model_start).setVisibility(View.GONE);
        findViewById(R.id.model_stop).setVisibility(View.GONE);
        findViewById(R.id.model_reset).setVisibility(View.GONE);
        stopThresholdButton.setOnClickListener(v -> {
            if (controlArmed || autoInferenceEnabled || inferenceInFlight) {
                normalStop(getString(R.string.reason_adjust_uavflow_stop_threshold));
            }
            stopThreshold = Math.round((stopThreshold >= 0.9 ? 0.1 : stopThreshold + 0.1) * 10.0) / 10.0;
            vlnPreferences.edit().putFloat("uavflow-stop-threshold", (float) stopThreshold).apply();
            renderStopThresholdButton(stopThresholdButton);
            appendLog(String.format(Locale.US, "MODEL UAVFlow stop threshold=%.1f", stopThreshold));
            showBanner(getString(R.string.uavflow_stop_threshold_value, stopThreshold));
        });
        findViewById(R.id.model_health).setOnClickListener(v -> runModelOperation("health"));
        findViewById(R.id.model_load).setOnClickListener(v -> runModelOperation("load"));
        findViewById(R.id.model_preflight).setOnClickListener(v -> runModelOperation("preflight"));
        findViewById(R.id.model_start).setOnClickListener(v -> runModelOperation("start"));
        findViewById(R.id.model_stop).setOnClickListener(v -> runModelOperation("stop"));
        findViewById(R.id.model_reset).setOnClickListener(v -> runModelOperation("reset"));
        findViewById(R.id.model_infer_once).setOnClickListener(v -> inferCurrentFrame());
        selectModelTransport(ModelTransport.LOCAL);
    }

    private void renderStopThresholdButton(Button button) {
        button.setText(getString(Math.abs(stopThreshold - 0.7) < 1.0e-6
                        ? R.string.uavflow_stop_threshold_value_default
                        : R.string.uavflow_stop_threshold_value,
                stopThreshold));
    }

    private void initDjiAccount() {
        djiAccountStatusText = findViewById(R.id.dji_account_status_text);
        djiAccountLoginButton = findViewById(R.id.dji_account_login_button);
        djiAccountController = new DjiUserAccountController(this,
                new DjiUserAccountController.Listener() {
                    @Override public void onChanged(DjiUserAccountController.Snapshot snapshot) {
                        renderDjiAccount(snapshot);
                    }

                    @Override public void onLoginResult(boolean success, String error) {
                        if (success) {
                            appendLog("DJI_ACCOUNT login_result=SUCCESS");
                            showBanner(getString(R.string.dji_account_login_success));
                        } else {
                            String detail = error == null ? "unknown" : error;
                            appendLog("DJI_ACCOUNT login_result=FAIL · " + detail);
                            showBanner(getString(R.string.dji_account_login_failed, detail));
                        }
                    }
                });
        djiAccountLoginButton.setOnClickListener(v -> {
            appendLog("DJI_ACCOUNT login_requested");
            djiAccountController.login();
        });
        renderDjiAccount(djiAccountController.snapshot());
    }

    private void renderDjiAccount(DjiUserAccountController.Snapshot snapshot) {
        if (djiAccountStatusText == null || djiAccountLoginButton == null) return;
        String base;
        if (snapshot.isLoggedIn()) {
            base = snapshot.maskedAccount == null
                    ? getString(R.string.dji_account_logged_in)
                    : getString(R.string.dji_account_logged_in_name, snapshot.maskedAccount);
        } else if (snapshot.state == UserAccountState.NOT_LOGGED_IN) {
            base = getString(R.string.dji_account_not_logged_in);
        } else if (snapshot.state == UserAccountState.TOKEN_OUT_OF_DATE) {
            base = getString(R.string.dji_account_expired);
        } else {
            base = getString(R.string.dji_account_checking);
        }
        if (snapshot.lastError != null && !snapshot.lastError.isEmpty()) {
            base = getString(R.string.dji_account_recent_error, base, snapshot.lastError);
        }
        djiAccountStatusText.setText(base);
        djiAccountStatusText.setTextColor(snapshot.isLoggedIn()
                ? getColor(R.color.text_ok_green)
                : getColor(R.color.text_accent_amber));
        djiAccountLoginButton.setText(snapshot.isLoggedIn()
                ? R.string.relogin_dji_account : R.string.login_dji_account);
        djiAccountLoginButton.setEnabled(true);
        String signature = snapshot.state.name() + ":" + snapshot.maskedAccount
                + ":" + snapshot.lastError;
        if (!signature.equals(lastDjiAccountLogState)) {
            lastDjiAccountLogState = signature;
            appendLog("DJI_ACCOUNT state=" + snapshot.state.name()
                    + (snapshot.maskedAccount == null ? "" : " account=" + snapshot.maskedAccount));
        }
    }

    private void initFullFlightUi(Bundle savedInstanceState) {
        flightStatusText = findViewById(R.id.flight_status_text);
        flightModeText = findViewById(R.id.flight_mode_text);
        gpsStatusText = findViewById(R.id.gps_status_text);
        rcStatusText = findViewById(R.id.rc_status_text);
        rcSignalView = findViewById(R.id.rc_signal_view);
        aircraftBatteryText = findViewById(R.id.aircraft_battery_text);
        rcBatteryText = findViewById(R.id.rc_battery_text);
        compassText = findViewById(R.id.compass_text);
        speedText = findViewById(R.id.speed_text);
        altitudeText = findViewById(R.id.altitude_text);
        distanceText = findViewById(R.id.distance_text);
        locationSourceText = findViewById(R.id.location_source_text);
        cameraModeText = findViewById(R.id.camera_mode_text);
        recordingTimeText = findViewById(R.id.recording_time_text);
        safetyGateText = findViewById(R.id.safety_gate_text);
        commandText = findViewById(R.id.command_text);
        advancedStateText = findViewById(R.id.advanced_state_text);
        logText = findViewById(R.id.log_text);
        toastBanner = findViewById(R.id.toast_banner);
        surveyPhotoCaptureFeedback = findViewById(R.id.survey_photo_capture_feedback);
        initDjiAccount();

        ScrollView logScroll = findViewById(R.id.log_scroll);
        logScroll.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (logAutoScrollInProgress || logText == null) return;
            int bottom = Math.max(0, logText.getHeight() - view.getHeight());
            boolean atLatest = scrollY >= bottom - dp(12);
            if (monitorFollow != atLatest) {
                monitorFollow = atLatest;
                renderMonitorFollowButton();
            }
        });

        findViewById(R.id.aircraft_status_button).setOnClickListener(v -> toggleVisibility(statusText));
        findViewById(R.id.more_button).setOnClickListener(v -> showAdvancedPanel(true));
        findViewById(R.id.advanced_toggle_button).setOnClickListener(v -> showAdvancedPanel(true));
        findViewById(R.id.vln_minimize_button).setOnClickListener(v -> toggleVlnPanel());
        findViewById(R.id.flight_panel_minimize_button).setOnClickListener(v -> toggleFlightPanel());
        findViewById(R.id.advanced_close_button).setOnClickListener(v -> showAdvancedPanel(false));
        findViewById(R.id.settings_tab_link).setOnClickListener(v -> selectSettingsTab("link"));
        findViewById(R.id.settings_tab_flight).setOnClickListener(v -> selectSettingsTab("flight"));
        findViewById(R.id.settings_tab_exec).setOnClickListener(v -> selectSettingsTab("exec"));
        findViewById(R.id.settings_tab_system).setOnClickListener(v -> selectSettingsTab("system"));
        findViewById(R.id.app_language_button).setOnClickListener(v ->
                AppLanguageController.showSettingsPicker(
                        this,
                        !aircraftSnapshot.getFlying()
                                && !aircraftSnapshot.getSimulatorMotorsOn()
                                && !hasActiveHilControlledOperation()));
        findViewById(R.id.model_monitor_button).setOnClickListener(v -> showMonitor(true));
        findViewById(R.id.monitor_close_button).setOnClickListener(v -> showMonitor(false));
        findViewById(R.id.monitor_clear_button).setOnClickListener(v -> {
            runtimeLog.setLength(0);
            runtimeLogLines = 0;
            logText.setText("");
        });
        findViewById(R.id.monitor_follow_button).setOnClickListener(v -> {
            monitorFollow = !monitorFollow;
            renderMonitorFollowButton();
            if (monitorFollow) scrollLogToLatest();
        });

        Button takeoff = findViewById(R.id.takeoff_button);
        takeoff.setOnClickListener(v -> showBanner(getString(R.string.takeoff_hold_required)));
        takeoff.setOnLongClickListener(v -> {
            confirmDangerousAction(getString(R.string.confirm_takeoff_title),
                    getString(R.string.confirm_takeoff_message), () -> {
                abortSurveySimulatorExecution(getString(R.string.reason_user_requested_takeoff), true);
                if ((hilController != null && hilController.isRunning())
                        || aircraftSnapshot.getSimulatorActive()) {
                    startHilUiSimulatorTakeoff();
                } else {
                    aircraftBridge.takeOff();
                }
            });
            return true;
        });
        findViewById(R.id.rth_button).setOnClickListener(v ->
            confirmDangerousAction(getString(R.string.confirm_rth_title),
                    getString(R.string.confirm_rth_message), () -> {
                    pauseSurveyForExternalIntervention(getString(R.string.reason_user_requested_rth), true);
                    aircraftBridge.startGoHome();
                }));
        findViewById(R.id.cancel_rth_button).setOnClickListener(v -> aircraftBridge.cancelGoHome());
        findViewById(R.id.land_button).setOnClickListener(v ->
            confirmDangerousAction(getString(R.string.action_confirm_landing),
                    getString(R.string.confirm_landing_message), () -> {
                    pauseSurveyForExternalIntervention(getString(R.string.reason_user_requested_landing), true);
                    aircraftBridge.startLanding();
                }));
        findViewById(R.id.cancel_land_button).setOnClickListener(v -> aircraftBridge.cancelLanding());
        findViewById(R.id.confirm_land_button).setOnClickListener(v -> aircraftBridge.confirmLanding());

        findViewById(R.id.shutter_button).setOnClickListener(v -> {
            final long[] triggerFrameId = {0L};
            aircraftBridge.takePhoto((triggeredAtNanos, triggeredAtEpochMillis) ->
                            triggerFrameId[0] = beginTriggerAlignedFrameCapture(
                                    "MANUAL_SHUTTER", triggeredAtNanos, triggeredAtEpochMillis),
                    (ok, message) -> runOnUiThread(() -> {
                completeTriggerAlignedFrameCapture(triggerFrameId[0], ok, message);
                showPhotoCaptureFeedback(ok);
            }));
        });
        findViewById(R.id.record_button).setOnClickListener(v -> aircraftBridge.toggleRecording());
        findViewById(R.id.gallery_button).setOnClickListener(v -> {
            if (aircraftSnapshot.getFlying()) {
                showBanner(getString(R.string.media_mode_switch_blocked_flying));
                return;
            }
            showGallery(true);
            refreshGallery();
        });
        findViewById(R.id.gallery_refresh_button).setOnClickListener(v -> refreshGallery());
        findViewById(R.id.gallery_close_button).setOnClickListener(v -> showGallery(false));
        findViewById(R.id.gallery_filter_all).setOnClickListener(v -> setMediaFilter("all"));
        findViewById(R.id.gallery_filter_photo).setOnClickListener(v -> setMediaFilter("photo"));
        findViewById(R.id.gallery_filter_video).setOnClickListener(v -> setMediaFilter("video"));
        initMediaGrid();

        initIosPromptPresets();

        findViewById(R.id.auto_infer_button).setOnClickListener(this::toggleAutoInference);
        findViewById(R.id.control_toggle_button).setOnClickListener(this::toggleFlightControl);
        findViewById(R.id.emergency_stop_button).setOnClickListener(v ->
                activateEmergencyStop(getString(R.string.reason_user_pressed_emergency_stop)));
        findViewById(R.id.model_load_main).setOnClickListener(v -> runModelOperation("load"));
        findViewById(R.id.normal_stop_button).setOnClickListener(v ->
                normalStop(getString(R.string.reason_user_stopped)));
        findViewById(R.id.reset_emergency_button).setOnClickListener(v -> resetEmergency());
        findViewById(R.id.clear_emergency_button).setOnClickListener(v -> {
            emergencyStopped = false;
            appendLog("SAFETY emergency stop cleared by user");
            showBanner(getString(R.string.emergency_lock_cleared_control_off));
            renderSafetyState();
        });
        findViewById(R.id.enable_vs_button).setOnClickListener(v -> {
            if (isSurveySimulatorControlReserved()) {
                showBanner(getString(R.string.survey_simulator_owns_vs));
                return;
            }
            if (!aircraftSnapshot.getConnected()) {
                showBanner(getString(R.string.vs_enable_flight_controller_disconnected));
                return;
            }
            aircraftBridge.enableVirtualStick();
        });
        findViewById(R.id.disable_vs_button).setOnClickListener(v -> {
            pauseSurveyForExternalIntervention(getString(R.string.reason_user_released_vs), false);
            controlArmed = false;
            aircraftBridge.disableVirtualStick(getString(R.string.reason_user_released));
            renderSafetyState();
        });
        findViewById(R.id.manual_xyz_button).setOnClickListener(v -> runManualXyz());
        findViewById(R.id.manual_xyz_stop_button).setOnClickListener(v ->
                normalStop(getString(R.string.reason_user_stopped_manual_xyz)));

        SeekBar speedSeek = findViewById(R.id.speed_limit_seek);
        android.content.SharedPreferences vlnPreferences = getSharedPreferences("vln", MODE_PRIVATE);
        speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && controlArmed) normalStop(getString(R.string.reason_adjust_vln_max_speed));
                speedLimitMetersPerSecond = 0.2f + progress * 0.1f;
                ((TextView) findViewById(R.id.speed_limit_text)).setText(String.format(Locale.US, "%.1f m/s", speedLimitMetersPerSecond));
                if (fromUser) vlnPreferences.edit().putFloat("maximum-horizontal-speed", speedLimitMetersPerSecond).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        float storedSpeed = Math.max(0.2f, Math.min(4.0f,
                vlnPreferences.getFloat("maximum-horizontal-speed", 1.0f)));
        speedSeek.setProgress(Math.round((storedSpeed - 0.2f) * 10f));
        findViewById(R.id.position_gps_button).setOnClickListener(v -> {
            if (!velocityEstimateMode) return;
            if (controlArmed) normalStop(getString(R.string.reason_switch_position_closure_mode));
            velocityEstimateMode = false;
            localRuntime().resetEstimatedModelState();
            showBanner(getString(R.string.position_loop_gps));
            renderSafetyState();
        });
        findViewById(R.id.position_velocity_button).setOnClickListener(v -> {
            if (velocityEstimateMode) return;
            if (controlArmed) normalStop(getString(R.string.reason_switch_position_closure_mode));
            velocityEstimateMode = true;
            localRuntime().resetEstimatedModelState();
            showBanner(getString(R.string.position_loop_velocity));
            renderSafetyState();
        });
        continuousChunkEnabled = vlnPreferences.getBoolean("uavflow-h5-execution", true);
        executedPrefix = Math.max(1, Math.min(UAVFlowPolicyContract.HORIZON,
                vlnPreferences.getInt("uavflow-executed-prefix", UAVFlowPolicyContract.DEFAULT_EXECUTED_PREFIX)));
        renderChunkConfiguration();
        findViewById(R.id.chunk_mode_button).setOnClickListener(v -> {
            continuousChunkEnabled = !continuousChunkEnabled;
            renderChunkConfiguration();
            vlnPreferences.edit().putBoolean("uavflow-h5-execution", continuousChunkEnabled).apply();
            if (!continuousChunkEnabled && chunkExecutionActive) {
                abortChunkExecution(getString(R.string.reason_user_disabled_uavflow_continuous_execution));
            }
            appendLog("MODEL UAVFlow H10/H" + executedPrefix + " execution=" + continuousChunkEnabled);
        });
        Spinner chunkStepsSpinner = findViewById(R.id.chunk_steps_spinner);
        String[] chunkStepLabels = new String[UAVFlowPolicyContract.HORIZON];
        for (int index = 0; index < chunkStepLabels.length; index++) {
            chunkStepLabels[index] = "H" + (index + 1);
        }
        ArrayAdapter<String> chunkStepsAdapter = new ArrayAdapter<>(
                this, R.layout.item_prompt_spinner, chunkStepLabels);
        chunkStepsAdapter.setDropDownViewResource(R.layout.item_prompt_spinner_dropdown);
        chunkStepsSpinner.setAdapter(chunkStepsAdapter);
        chunkStepsSpinner.setSelection(executedPrefix - 1, false);
        chunkStepsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedPrefix = position + 1;
                if (selectedPrefix == executedPrefix) return;
                if (controlArmed || autoInferenceEnabled || inferenceInFlight || chunkExecutionActive) {
                    normalStop(getString(R.string.reason_adjust_uavflow_execution_steps));
                }
                executedPrefix = selectedPrefix;
                vlnPreferences.edit().putInt("uavflow-executed-prefix", executedPrefix).apply();
                renderChunkConfiguration();
                appendLog(getString(R.string.uavflow_execution_prefix_log, executedPrefix));
                renderSafetyState();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        flyThroughEnabled = vlnPreferences.getBoolean("fly-through", true);
        ((Button) findViewById(R.id.flythrough_mode_button)).setText(
                flyThroughEnabled ? R.string.vln_flythrough_on : R.string.vln_flythrough_off);
        findViewById(R.id.flythrough_mode_button).setOnClickListener(v -> {
            if (controlArmed) normalStop(getString(R.string.reason_switch_fly_through_mode));
            flyThroughEnabled = !flyThroughEnabled;
            ((Button) v).setText(flyThroughEnabled
                    ? R.string.vln_flythrough_on : R.string.vln_flythrough_off);
            vlnPreferences.edit().putBoolean("fly-through", flyThroughEnabled).apply();
            appendLog("CONTROL fly-through=" + flyThroughEnabled);
            renderSafetyState();
        });
        findViewById(R.id.simulator_toggle_button).setOnClickListener(v -> {
            boolean enabled = !aircraftSnapshot.getSimulatorActive();
            SurveyExecutionState surveyState = surveySimulatorExecution == null
                    ? null : surveySimulatorExecution.getStatus().getState();
            if (SurveySimulatorSwitchPolicy.INSTANCE.actionFor(surveyState)
                    == SurveySimulatorSwitchAction.PAUSE_AND_PRESERVE_CHECKPOINT) {
                pauseSurveyForExternalIntervention(
                        getString(R.string.reason_switch_dji_builtin_simulator), true);
                showBanner(getString(R.string.simulator_switch_route_paused_checkpoint));
            } else if (surveyState == SurveyExecutionState.PAUSED) {
                appendLog("SURVEY remains PAUSED during DJI Simulator switch · checkpoint retained");
                showBanner(getString(R.string.simulator_switch_paused_checkpoint_preserved));
            }
            boolean otherControllerActive = controlArmed || autoInferenceEnabled || inferenceInFlight
                    || chunkExecutionActive || activeRelativeMoveRunnable != null;
            if (otherControllerActive) {
                String reason = getString(R.string.reason_switch_dji_builtin_simulator);
                if (SurveySimulatorSwitchPolicy.INSTANCE.reservesControl(surveyState)) {
                    stopVlnControl(reason, false);
                } else {
                    normalStop(reason);
                }
            }
            if (!enabled && hilController != null && hilController.isRunning()) {
                hilVirtualFramesEnabled = false;
                String reason = getString(R.string.reason_user_disabled_dji_simulator);
                invalidateHilSimulatorActivation(reason);
                pauseHilForTransientSourceLoss(reason);
                renderHilStatus();
                appendLog(getString(R.string.hil_network_kept_during_simulator_switch));
            }
            aircraftBridge.setSimulatorEnabled(enabled);
        });
        findViewById(R.id.rc_phone_charging_button).setOnClickListener(v -> {
            boolean enabled = !"ALWAYS".equals(aircraftSnapshot.getRcPhoneChargingMode());
            appendLog("RC phone charging requested=" + enabled);
            aircraftBridge.setPhoneChargingEnabled(enabled, (ok, message) -> {
                appendLog("RC phone charging result=" + (ok ? "OK" : "FAIL")
                        + " requested=" + enabled + " · " + message);
            });
        });

        mapView = findViewById(R.id.map_view);
        amap = mapView.getMap();
        if (amap != null) {
            mapView.showZoomControls(false);
            amap.getUiSettings().setCompassEnabled(false);
            mapView.showScaleControl(false);
            amap.getUiSettings().setOverlookingGesturesEnabled(true);
            amap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            amap.setBuildingsEnabled(true);
            amap.setOnMapLoadedCallback(() -> {
                mapBaseLoaded = true;
                renderMapLocationStatus(aircraftSnapshot);
                MapStatus camera = amap.getMapStatus();
                appendLog(String.format(Locale.US,
                        "MAP loaded center=%.5f,%.5f zoom=%.1f",
                        camera.target.latitude, camera.target.longitude, camera.zoom));
            });
            amap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                @Override public void onMapClick(LatLng point) {
                    if (!surveyPlanningActive) return;
                    if (rejectSurveyEditingIfLocked()) return;
                    GeoPoint wgs84 = ChinaCoordinateTransform.INSTANCE.gcj02ToWgs84(
                            new GeoPoint(point.latitude, point.longitude, 0.0));
                    abortSurveySimulatorExecution(getString(R.string.reason_edit_survey_boundary), true);
                    stopSurveyReplay(true);
                    if (selectedSurveyVertexIndex >= 0 && selectedSurveyVertexIndex < surveyRoi.size()) {
                        int movedIndex = selectedSurveyVertexIndex;
                        surveyRoi.set(movedIndex, wgs84);
                        selectedSurveyVertexIndex = -1;
                        surveyMission = null;
                        clearPersistedSurveySession();
                        updateSuggestedSurveyHeadingFromRoi();
                        renderSurveyOverlay();
                        renderSurveyStatus(getString(R.string.survey_boundary_moved, movedIndex + 1));
                        return;
                    }
                    surveyRoi.add(wgs84);
                    surveyMission = null;
                    clearPersistedSurveySession();
                    updateSuggestedSurveyHeadingFromRoi();
                    renderSurveyOverlay();
                    renderSurveyStatus(getString(R.string.survey_boundary_added, surveyRoi.size()));
                }
                @Override public void onMapPoiClick(com.baidu.mapapi.map.MapPoi point) {
                    // Baidu dispatches labelled roads/buildings/POIs here instead of
                    // onMapClick. Treat that position as an ordinary survey tap so an
                    // urban basemap does not appear to ignore most touches.
                    if (point != null && point.getPosition() != null) {
                        onMapClick(point.getPosition());
                    }
                }
            });
            amap.setOnMarkerClickListener(marker -> {
                int index = surveyVertexMarkers.indexOf(marker);
                if (!surveyPlanningActive || index < 0) return false;
                selectedSurveyVertexIndex = index;
                marker.setTitle(getString(R.string.survey_boundary_marker_title, index + 1));
                marker.showInfoWindow();
                renderSurveyStatus(getString(R.string.survey_boundary_selected, index + 1));
                return true;
            });
            frameMapNearPhone();
            updateMap(aircraftSnapshot);
            renderSurveyOverlay();
            renderSurveyTerrainMapOverlay();
        }
        findViewById(R.id.map_toggle_button).setOnClickListener(v -> {
            if (surveyPlanningActive) showSurveyPlanner(false);
            else toggleMapFullscreen();
        });
        findViewById(R.id.map_layer_button).setOnClickListener(v -> {
            mapSatellite = !mapSatellite;
            ((Button) v).setText(mapSatellite
                    ? R.string.map_street_compact : R.string.map_satellite_compact);
            if (amap != null) {
                amap.setMapType(mapSatellite ? BaiduMap.MAP_TYPE_SATELLITE : BaiduMap.MAP_TYPE_NORMAL);
            }
        });
        findViewById(R.id.survey_button).setOnClickListener(v -> showSurveyPlanner(true));
        findViewById(R.id.v86_open_button).setOnClickListener(v -> showV86Dialog());
        findViewById(R.id.survey_frame_all_button).setOnClickListener(v -> frameCompleteSurvey());
        findViewById(R.id.survey_map_3d_button).setOnClickListener(v ->
                setSurveyMapThreeDimensional(!mapThreeDimensional));
        findViewById(R.id.survey_close_button).setOnClickListener(v -> showSurveyPlanner(false));
        findViewById(R.id.survey_header_more_button).setOnClickListener(v ->
                showSurveyPlannerMoreMenu());
        findViewById(R.id.survey_execution_status_text).setOnClickListener(v ->
                showSurveyExecutionStatusDetails());
        findViewById(R.id.survey_tab_area_button).setOnClickListener(v ->
                showSurveyPlannerTab(SURVEY_TAB_AREA));
        findViewById(R.id.survey_tab_route_button).setOnClickListener(v ->
                showSurveyPlannerTab(SURVEY_TAB_ROUTE));
        findViewById(R.id.survey_tab_capture_button).setOnClickListener(v ->
                showSurveyPlannerTab(SURVEY_TAB_CAPTURE));
        findViewById(R.id.survey_tab_terrain_button).setOnClickListener(v ->
                showSurveyPlannerTab(SURVEY_TAB_TERRAIN));
        findViewById(R.id.survey_header_pause_button).setOnClickListener(v -> {
            if (surveySimulatorExecution != null
                    && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.PAUSED) {
                startSurveySimulatorExecution();
            } else {
                pauseSurveySimulatorExecution();
            }
        });
        findViewById(R.id.survey_center_aircraft_button).setOnClickListener(v ->
                frameMapAtAircraft());
        findViewById(R.id.survey_undo_button).setOnClickListener(v -> {
            if (rejectSurveyEditingIfLocked()) return;
            abortSurveySimulatorExecution(getString(R.string.reason_edit_survey_boundary), true);
            stopSurveyReplay(true);
            if (!surveyRoi.isEmpty()) surveyRoi.remove(surveyRoi.size() - 1);
            selectedSurveyVertexIndex = -1;
            surveyMission = null;
            clearPersistedSurveySession();
            updateSuggestedSurveyHeadingFromRoi();
            renderSurveyOverlay();
            renderSurveyStatus(getString(R.string.survey_boundary_count_continue, surveyRoi.size()));
        });
        findViewById(R.id.survey_delete_vertex_button).setOnClickListener(v -> {
            if (rejectSurveyEditingIfLocked()) return;
            abortSurveySimulatorExecution(getString(R.string.reason_delete_survey_boundary_point), true);
            stopSurveyReplay(true);
            if (selectedSurveyVertexIndex < 0 || selectedSurveyVertexIndex >= surveyRoi.size()) {
                renderSurveyStatus(getString(R.string.survey_select_boundary_before_delete));
                return;
            }
            int removedIndex = selectedSurveyVertexIndex;
            surveyRoi.remove(removedIndex);
            selectedSurveyVertexIndex = -1;
            surveyMission = null;
            clearPersistedSurveySession();
            updateSuggestedSurveyHeadingFromRoi();
            renderSurveyOverlay();
            renderSurveyStatus(getString(R.string.survey_boundary_deleted,
                    removedIndex + 1, surveyRoi.size()));
        });
        findViewById(R.id.survey_clear_button).setOnClickListener(v -> {
            if (rejectSurveyEditingIfLocked()) return;
            if (surveyRoi.isEmpty() && surveyMission == null) {
                renderSurveyStatus(getString(R.string.survey_nothing_to_clear));
                return;
            }
            confirmDangerousAction(getString(R.string.survey_clear_route_title),
                    getString(R.string.survey_clear_route_message), () -> {
                        abortSurveySimulatorExecution(getString(R.string.reason_clear_survey_mission), true);
                        stopSurveyReplay(true);
                        surveyRoi.clear();
                        selectedSurveyVertexIndex = -1;
                        surveyMission = null;
                        clearPersistedSurveySession();
                        renderSurveyOverlay();
                        renderSurveyStatus(getString(R.string.survey_cleared_add_boundary));
                    });
        });
        findViewById(R.id.survey_grid_button).setOnClickListener(v -> {
            surveySpeedHintFiveDirection = false;
            updateSurveySpeedLimitHint();
            schedulePersistSurveyPlannerSettings();
            generateSurveyMission(false);
        });
        findViewById(R.id.survey_crosshatch_button).setOnClickListener(v -> {
            appendLog("SURVEY generate selected groups=" + surveyCaptureViewSelectionLabel());
            surveySpeedHintFiveDirection = true;
            updateSurveySpeedLimitHint();
            schedulePersistSurveyPlannerSettings();
            generateSurveyMission(true);
        });
        findViewById(R.id.survey_oblique_angle_button).setOnClickListener(v ->
                showSurveyObliqueAngleDialog());
        findViewById(R.id.survey_rth_height_set_button).setOnClickListener(v ->
                setSurveyGoHomeHeight());
        surveySettingsRestoring = true;
        setupSurveyOptionSpinners();
        setupSurveySpinner(R.id.survey_terrain_kind_spinner,
                new String[]{getString(R.string.terrain_kind_dsm), getString(R.string.terrain_kind_dem)});
        setupSurveyGsdAltitudeLink();
        setupSurveyImeActions();
        restoreSurveyPlannerSettings();
        surveySettingsRestoring = false;
        setupSurveyPlannerSettingsPersistence();
        ((CheckBox) findViewById(R.id.survey_terrain_enabled_checkbox))
                .setOnCheckedChangeListener((button, enabled) ->
                        setSurveyTerrainFollowingEnabled(enabled, true));
        CheckBox terrainEnabled = findViewById(R.id.survey_terrain_enabled_checkbox);
        terrainEnabled.setTextColor(0xFF244A68);
        terrainEnabled.setAlpha(1.0f);
        renderSurveyObliqueAngleButton();
        renderSurveyTerrainModeControl();
        findViewById(R.id.survey_replay_button).setOnClickListener(v -> toggleSurveyReplay());
        findViewById(R.id.survey_replay_stop_button).setOnClickListener(v -> stopSurveyReplay(true));
        findViewById(R.id.survey_import_button).setOnClickListener(v -> chooseSurveyMission());
        findViewById(R.id.survey_dsm_import_button).setOnClickListener(v -> chooseSurveyDsm());
        findViewById(R.id.survey_global_terrain_button).setOnClickListener(v -> {
            appendLog("SURVEY terrain download requested by user");
            downloadGlobalSurveyTerrain();
        });
        Button buildingHeightButton = findViewById(R.id.survey_global_buildings_button);
        buildingHeightButton.setText(BuildConfig.GLOBAL_BUILDING_HEIGHT_COG_TEMPLATE.trim().isEmpty()
                ? getString(R.string.import_building_height) : getString(R.string.download_building_height));
        buildingHeightButton.setOnClickListener(v -> {
            appendLog(BuildConfig.GLOBAL_BUILDING_HEIGHT_COG_TEMPLATE.trim().isEmpty()
                    ? "SURVEY building-height file requested by user"
                    : "SURVEY building-height download requested by user");
            downloadGlobalBuildingHeights();
        });
        findViewById(R.id.survey_export_button).setOnClickListener(v -> exportSurveyMission());
        findViewById(R.id.survey_library_button).setOnClickListener(v -> showSurveyMissionLibrary());
        findViewById(R.id.survey_save_version_button).setOnClickListener(v ->
                saveSurveyMissionVersion(true));
        findViewById(R.id.survey_view_nadir_button).setOnClickListener(v ->
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.NADIR));
        findViewById(R.id.survey_view_forward_button).setOnClickListener(v ->
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.FORWARD_OBLIQUE));
        findViewById(R.id.survey_view_backward_button).setOnClickListener(v ->
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.BACKWARD_OBLIQUE));
        findViewById(R.id.survey_view_left_button).setOnClickListener(v ->
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.LEFT_OBLIQUE));
        findViewById(R.id.survey_view_right_button).setOnClickListener(v ->
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.RIGHT_OBLIQUE));
        findViewById(R.id.survey_active_recapture_groups_button).setOnClickListener(v ->
                showActiveRecaptureGroupDialog());
        findViewById(R.id.survey_sim_gate_button).setOnClickListener(v -> runSurveySimulatorGateCheck());
        findViewById(R.id.survey_sim_start_button).setOnClickListener(v -> startSurveySimulatorExecution());
        findViewById(R.id.survey_sim_pause_button).setOnClickListener(v -> pauseSurveySimulatorExecution());
        findViewById(R.id.survey_sim_abort_button).setOnClickListener(v ->
                abortSurveySimulatorExecution(getString(R.string.reason_user_stopped_simulated_route), true));
        findViewById(R.id.survey_planner_preflight_button).setOnClickListener(v ->
                runSurveySimulatorGateCheck());
        findViewById(R.id.survey_planner_execute_button).setOnClickListener(v ->
                startSurveySimulatorExecution());
        findViewById(R.id.survey_planner_abort_button).setOnClickListener(v ->
                abortSurveySimulatorExecution(getString(R.string.reason_user_stopped_mission_from_route_panel), true));
        EditText ueEndpoint = findViewById(R.id.ue_bridge_endpoint);
        android.content.SharedPreferences bridgePreferences = getSharedPreferences("ue-bridge", MODE_PRIVATE);
        ueEndpoint.setText(bridgePreferences.getString("endpoint", "http://192.168.1.2:30010"));
        findViewById(R.id.ue_bridge_send_mission).setOnClickListener(v -> sendSurveyMissionToUe());
        findViewById(R.id.ue_bridge_toggle).setOnClickListener(v -> toggleUeBridge());
        initHilUi();
        findViewById(R.id.survey_real_readiness_button).setOnClickListener(v ->
                auditSurveyRealFlightReadiness());
        applyV5VisualStyle();
        // Generic V5 styling runs after HIL initialization, so restore the two
        // stateful HIL controls once the common button pass has finished.
        renderHilConnectionMode();
        renderHilStatus();
        selectSettingsTab("link");
        appendLog("UI full iOS/V5 flight HUD initialized");
    }

    private void applyV5VisualStyle() {
        View root = findViewById(R.id.root_hud);
        styleButtonsRecursively(root);

        int neutralFill = getColor(R.color.btn_neutral_fill);
        int neutralStroke = getColor(R.color.btn_neutral_stroke);
        int primaryFill = getColor(R.color.btn_primary_fill);
        int primaryStroke = getColor(R.color.btn_primary_stroke);
        int dangerFill = getColor(R.color.btn_danger_fill);
        int dangerStroke = getColor(R.color.btn_danger_stroke);

        int[] primary = new int[] {
                R.id.model_load_main, R.id.model_health, R.id.model_load, R.id.model_preflight, R.id.model_start,
                R.id.download_cloud_model
        };
        for (int id : primary) styleButton(findViewById(id), primaryFill, primaryStroke, 10);
        int[] danger = new int[] {R.id.emergency_stop_button, R.id.model_stop, R.id.disable_vs_button, R.id.manual_xyz_stop_button};
        for (int id : danger) styleButton(findViewById(id), dangerFill, dangerStroke, 10);
        int[] neutral = new int[] {
                R.id.rth_button, R.id.cancel_rth_button, R.id.land_button, R.id.cancel_land_button,
                R.id.model_infer_once, R.id.auto_infer_button, R.id.control_toggle_button,
                R.id.normal_stop_button, R.id.reset_emergency_button
        };
        for (int id : neutral) styleButton(findViewById(id), neutralFill, neutralStroke, 10);
        styleButton(findViewById(R.id.confirm_land_button), 0xE64A3D22, 0xCCF7C66A, 10);
        styleButton(findViewById(R.id.takeoff_button), primaryFill, primaryStroke, 30);
        ((Button) findViewById(R.id.takeoff_button)).setTextSize(22f);
        StateListDrawable shutterStates = new StateListDrawable();
        shutterStates.addState(new int[] {android.R.attr.state_pressed}, getDrawable(R.drawable.bg_shutter_pressed));
        shutterStates.addState(new int[0], getDrawable(R.drawable.bg_shutter));
        findViewById(R.id.shutter_button).setBackground(shutterStates);
        styleButton(findViewById(R.id.record_button), primaryFill, primaryStroke, 9);
        styleButton(findViewById(R.id.gallery_button), neutralFill, neutralStroke, 9);
        findViewById(R.id.aircraft_status_button).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.more_button).setBackgroundColor(Color.TRANSPARENT);

        int glassTop = getColor(R.color.glass_top);
        int glassBottom = getColor(R.color.glass_bottom);
        int topBarGlass = getColor(R.color.glass_top_bar);
        int strongTop = getColor(R.color.glass_strong_top);
        int strongBottom = getColor(R.color.glass_strong_bottom);
        int drawerTop = getColor(R.color.glass_drawer_top);
        int drawerBottom = getColor(R.color.glass_drawer_bottom);
        int stroke = getColor(R.color.glass_stroke);
        int strokeStrong = getColor(R.color.glass_stroke_strong);
        setPanelBackground(R.id.top_status_bar, topBarGlass, topBarGlass, stroke, 12);
        setPanelBackground(R.id.flight_control_panel, strongTop, strongBottom, stroke, 11);
        findViewById(R.id.navigation_hud).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.camera_action_rail).setBackgroundColor(Color.TRANSPARENT);
        styleMapOverlayButton(R.id.map_layer_button);
        styleMapOverlayButton(R.id.survey_frame_all_button);
        styleMapOverlayButton(R.id.survey_map_3d_button);
        styleMapOverlayButton(R.id.survey_button);
        styleMapOverlayButton(R.id.map_toggle_button);
        setPanelBackground(R.id.vln_panel, strongTop, strongBottom, strokeStrong, 11);
        setPanelBackground(R.id.advanced_panel, 0xFF202632, 0xFF141822, stroke, 0);
        setPanelBackground(R.id.model_monitor_panel, drawerTop, drawerBottom, stroke, 14);
        setPanelBackground(R.id.gallery_panel, 0xFF202632, 0xFF141822, stroke, 16);
        setPanelBackground(R.id.map_pane, glassTop, glassBottom, strokeStrong, 14);
        findViewById(R.id.map_pane).setClipToOutline(true);
        styleSurveyPlannerControls();
        renderTransportStyles();
    }

    private void styleMapOverlayButton(int viewId) {
        Button button = findViewById(viewId);
        if (button == null) return;
        button.setBackgroundResource(R.drawable.map_overlay_button);
        button.setTextColor(Color.WHITE);
        button.setTypeface(button.getTypeface(), android.graphics.Typeface.BOLD);
        button.setElevation(dp(4));
    }

    private void styleSurveyPlannerControls() {
        int lightFill = 0xFFF3F5F7;
        int lightStroke = 0xFFD8DDE3;
        int darkText = 0xFF343A40;
        int[] secondary = new int[] {
                R.id.survey_close_button, R.id.survey_undo_button, R.id.survey_clear_button,
                R.id.survey_delete_vertex_button,
                R.id.survey_replay_button, R.id.survey_replay_stop_button,
                R.id.survey_import_button, R.id.survey_export_button,
                R.id.survey_library_button, R.id.survey_save_version_button,
                R.id.survey_view_nadir_button, R.id.survey_view_forward_button,
                R.id.survey_view_backward_button, R.id.survey_view_left_button,
                R.id.survey_view_right_button, R.id.survey_center_aircraft_button,
                R.id.survey_header_more_button, R.id.survey_planner_preflight_button,
                R.id.survey_oblique_angle_button, R.id.survey_active_recapture_groups_button
        };
        for (int id : secondary) {
            Button button = findViewById(id);
            if (button == null) continue;
            styleButton(button, lightFill, lightStroke, 5);
            button.setTextColor(darkText);
        }
        Button close = findViewById(R.id.survey_close_button);
        if (close != null) {
            styleButton(close, 0xFFFFFFFF, 0x00FFFFFF, 18);
            close.setTextColor(0xFF555D66);
            close.setTextSize(20f);
        }
        Button grid = findViewById(R.id.survey_grid_button);
        if (grid != null) {
            styleButton(grid, 0xFF3478C6, 0xFF2768B2, 5);
            grid.setTextColor(0xFFFFFFFF);
        }
        Button crosshatch = findViewById(R.id.survey_crosshatch_button);
        if (crosshatch != null) {
            styleButton(crosshatch, 0xFF5C6672, 0xFF4D5661, 5);
            crosshatch.setTextColor(0xFFFFFFFF);
        }
        Button execute = findViewById(R.id.survey_planner_execute_button);
        if (execute != null) {
            styleButton(execute, 0xFF3478C6, 0xFF2768B2, 5);
            execute.setTextColor(0xFFFFFFFF);
        }
        Button abort = findViewById(R.id.survey_planner_abort_button);
        if (abort != null) {
            styleButton(abort, 0xFFFDECEC, 0xFFE49A9A, 5);
            abort.setTextColor(0xFFB3261E);
        }
        renderSurveyPlannerTabs();
        renderSurveyRoutePreviewStyles();
    }

    private void toggleSurveyCaptureViewEnabled(SurveyCaptureView captureView) {
        if (surveyMission != null && surveyMission.getActiveMapping() != null) {
            showBanner(getString(R.string.recapture_groups_fixed_banner));
            renderSurveyStatus(getString(R.string.recapture_groups_fixed_status));
            return;
        }
        if (surveyEnabledCaptureViews.contains(captureView)) {
            if (surveyEnabledCaptureViews.size() == 1) {
                renderSurveyStatus(getString(R.string.survey_keep_one_group));
                return;
            }
            surveyEnabledCaptureViews.remove(captureView);
        } else {
            surveyEnabledCaptureViews.add(captureView);
        }
        renderSurveyRoutePreviewStyles();
        schedulePersistSurveyPlannerSettings();
        if (surveyMission != null && surveyMission.getConstraints().getCollectionMode()
                == SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION) {
            if (surveyCaptureSelectionSourceMission == null
                    || !surveyCaptureSelectionSourceMission.getConstraints()
                    .getEnabledCaptureViews().containsAll(surveyEnabledCaptureViews)) {
                surveyCaptureSelectionSourceMission = surveyMission;
            }
            try {
                SurveyMission selectedMission = SurveyMissionCaptureViewFilter.select(
                        surveyCaptureSelectionSourceMission,
                        new HashSet<>(surveyEnabledCaptureViews), this);
                completeGeneratedSurveyMission(selectedMission, null, surveyTerrainPreviewData);
                showBanner(getString(R.string.survey_generated_selected_groups,
                        surveyEnabledCaptureViews.size()));
                return;
            } catch (IllegalArgumentException error) {
                appendLog("SURVEY selected group apply failed · " + error);
                showSurveyGenerationFailure(getString(R.string.survey_apply_selected_strips_failed,
                        error.getMessage()));
                return;
            }
        }
        renderSurveyStatus(getString(R.string.survey_five_direction_groups_status,
                surveyCaptureViewSelectionLabel()));
    }

    private void renderSurveyRoutePreviewStyles() {
        styleSurveyRoutePreviewButton(R.id.survey_view_nadir_button, SurveyCaptureView.NADIR);
        styleSurveyRoutePreviewButton(R.id.survey_view_forward_button, SurveyCaptureView.FORWARD_OBLIQUE);
        styleSurveyRoutePreviewButton(R.id.survey_view_backward_button, SurveyCaptureView.BACKWARD_OBLIQUE);
        styleSurveyRoutePreviewButton(R.id.survey_view_left_button, SurveyCaptureView.LEFT_OBLIQUE);
        styleSurveyRoutePreviewButton(R.id.survey_view_right_button, SurveyCaptureView.RIGHT_OBLIQUE);
        Button generate = findViewById(R.id.survey_crosshatch_button);
        if (generate != null) {
            generate.setEnabled(!surveyEnabledCaptureViews.isEmpty());
            generate.setText(getString(R.string.survey_generate_selected_groups,
                    surveyEnabledCaptureViews.size()));
            styleButton(generate, 0xFF3478C6, 0xFF2768B2, 5);
            generate.setTextColor(0xFFFFFFFF);
        }
        renderActiveRecaptureGroupButton();
    }

    private void renderActiveRecaptureGroupButton() {
        Button button = findViewById(R.id.survey_active_recapture_groups_button);
        if (button == null) return;
        SurveyMission current = surveyMission;
        if (current == null || current.getActiveMapping() == null) {
            button.setVisibility(View.GONE);
            return;
        }
        button.setVisibility(View.VISIBLE);
        SurveyMission source = activeRecaptureSourceMission == null
                ? current : activeRecaptureSourceMission;
        List<edu.playground.djivln.survey.ActiveRecaptureMissionGroup> groups =
                edu.playground.djivln.survey.ActiveRecaptureMissionGroupCatalog.groups(source, this);
        Set<String> selected =
                edu.playground.djivln.survey.ActiveRecaptureMissionGroupCatalog
                        .selectedGroupIds(source, current, this);
        button.setText(selected.size() == groups.size()
                ? getString(R.string.survey_recapture_groups_all_count, groups.size())
                : getString(R.string.survey_recapture_groups_selected_count,
                        selected.size(), groups.size()));
    }

    private void showActiveRecaptureGroupDialog() {
        if (rejectSurveyEditingIfLocked()) return;
        SurveyMission current = surveyMission;
        if (current == null || current.getActiveMapping() == null) {
            renderSurveyStatus(getString(R.string.recapture_not_active_mission));
            return;
        }
        SurveyMission source = activeRecaptureSourceMission == null
                ? current : activeRecaptureSourceMission;
        List<edu.playground.djivln.survey.ActiveRecaptureMissionGroup> groups =
                edu.playground.djivln.survey.ActiveRecaptureMissionGroupCatalog.groups(source, this);
        Set<String> selected =
                edu.playground.djivln.survey.ActiveRecaptureMissionGroupCatalog
                        .selectedGroupIds(source, current, this);
        boolean[] checked = new boolean[groups.size()];
        String[] labels = new String[groups.size()];
        for (int index = 0; index < groups.size(); index++) {
            edu.playground.djivln.survey.ActiveRecaptureMissionGroup group = groups.get(index);
            checked[index] = selected.contains(group.getGroupId());
            labels[index] = getString(R.string.recapture_group_label_with_photo_count,
                    group.getOrder(), group.getLabel(), group.getSuggestedSurveyPhotos());
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.survey_select_recapture_groups)
                .setMultiChoiceItems(labels, checked,
                        (ignored, which, enabled) -> checked[which] = enabled)
                .setNegativeButton(R.string.action_cancel, null)
                .setNeutralButton(R.string.action_select_all, null)
                .setPositiveButton(R.string.action_apply, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                for (int index = 0; index < checked.length; index++) {
                    checked[index] = true;
                    dialog.getListView().setItemChecked(index, true);
                }
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Set<String> selection = new HashSet<>();
                for (int index = 0; index < checked.length; index++) {
                    if (checked[index]) selection.add(groups.get(index).getGroupId());
                }
                if (selection.isEmpty()) {
                    renderSurveyStatus(getString(R.string.recapture_select_at_least_one));
                    return;
                }
                try {
                    SurveyMission filtered =
                            edu.playground.djivln.survey.ActiveRecaptureMissionRegionFilter
                                    .selectGroups(source, selection, this);
                    activeRecaptureSourceMission = source;
                    completeGeneratedSurveyMission(filtered, null, surveyTerrainPreviewData);
                    renderSurveyStatus(getString(R.string.recapture_groups_selected_status,
                            selection.size(), groups.size()));
                    appendLog("SURVEY active recapture groups selected " + selection);
                    dialog.dismiss();
                } catch (Throwable error) {
                    renderSurveyStatus(getString(R.string.recapture_apply_failed, error.getMessage()));
                    appendLog("SURVEY active recapture selection failed " + error);
                }
            });
        });
        dialog.show();
    }

    private void styleSurveyRoutePreviewButton(int id, SurveyCaptureView captureView) {
        Button button = findViewById(id);
        if (button == null) return;
        boolean selected = surveyEnabledCaptureViews.contains(captureView);
        styleButton(button, selected ? 0xFF3478C6 : 0xFFF3F5F7,
                selected ? 0xFF2768B2 : 0xFFD8DDE3, 5);
        button.setTextColor(selected ? 0xFFFFFFFF : 0xFF343A40);
    }

    private String surveyCaptureViewSelectionLabel() {
        return surveyCaptureViewSelectionLabel(surveyEnabledCaptureViews);
    }

    private String surveyCaptureViewSelectionLabel(Set<SurveyCaptureView> views) {
        StringBuilder label = new StringBuilder();
        SurveyCaptureView[] order = SurveyCaptureView.values();
        String[] names = new String[]{
                getString(R.string.capture_view_nadir_short),
                getString(R.string.capture_view_front_short),
                getString(R.string.capture_view_back_short),
                getString(R.string.capture_view_left_short),
                getString(R.string.capture_view_right_short)
        };
        for (int index = 0; index < names.length; index++) {
            if (!views.contains(order[index])) continue;
            if (label.length() > 0) label.append("+");
            label.append(names[index]);
        }
        return label.length() == 0 ? getString(R.string.selection_none) : label.toString();
    }

    private void styleButtonsRecursively(View view) {
        if (view instanceof Button) {
            Button button = (Button) view;
            button.setAllCaps(false);
            button.setTextSize(10f);
            button.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            button.setTextColor(getColor(R.color.text_primary));
            button.setMinHeight(0);
            button.setMinimumHeight(0);
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setPadding(dp(5), 0, dp(5), 0);
            styleButton(button, getColor(R.color.btn_neutral_fill), getColor(R.color.btn_neutral_stroke), 10);
            button.setOnTouchListener((target, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    target.animate().scaleX(0.96f).scaleY(0.96f).setDuration(45L).start();
                } else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    target.animate().scaleX(1f).scaleY(1f).setDuration(70L).start();
                }
                return false;
            });
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) styleButtonsRecursively(group.getChildAt(i));
        }
    }

    private void styleButton(View view, int fill, int stroke, int radiusDp) {
        if (view == null) return;
        GradientDrawable normal = gradientButtonDrawable(fill, shiftColor(fill, 1.14f), stroke, radiusDp);
        GradientDrawable pressed = gradientButtonDrawable(shiftColor(fill, 1.22f), shiftColor(fill, 1.38f), 0x66FFFFFF, radiusDp);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed}, pressed);
        states.addState(new int[0], normal);
        view.setBackground(states);
    }

    private GradientDrawable gradientButtonDrawable(int bottom, int top, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[] {top, bottom});
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(Math.max(1, dp(1)), stroke);
        return drawable;
    }

    private int shiftColor(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, Math.round(((color >>> 16) & 0xFF) * factor));
        int g = Math.min(255, Math.round(((color >>> 8) & 0xFF) * factor));
        int b = Math.min(255, Math.round((color & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void setPanelBackground(int id, int top, int bottom, int stroke, int radiusDp) {
        View view = findViewById(id);
        if (view == null) return;
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[] {top, bottom});
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(Math.max(1, dp(1)), stroke);
        view.setBackground(drawable);
        view.setElevation(dp(4));
    }

    private void renderTransportStyles() {
        if (localTransportButton == null) return;
        styleButton(localTransportButton,
                modelTransport == ModelTransport.LOCAL ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                modelTransport == ModelTransport.LOCAL ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
        styleButton(usbTransportButton,
                modelTransport == ModelTransport.USB ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                modelTransport == ModelTransport.USB ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
        styleButton(ethernetTransportButton,
                modelTransport == ModelTransport.ETHERNET ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                modelTransport == ModelTransport.ETHERNET ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
    }

    private void initAircraftBridge() {
        aircraftBridge = new Mini2AircraftBridge(new Mini2AircraftBridge.Listener() {
            @Override public void onSnapshot(Mini2AircraftBridge.Snapshot snapshot) {
                if (activityDestroyed || mockUiActive) return;
                Mini2AircraftBridge.Snapshot previousSnapshot = aircraftSnapshot;
                boolean newlyConnected = !previousSnapshot.getConnected() && snapshot.getConnected();
                boolean simulatorRestarted = !previousSnapshot.getSimulatorActive()
                        && snapshot.getSimulatorActive();
                aircraftSnapshot = snapshot;
                if (newlyConnected || simulatorRestarted) {
                    hilSimulatorCommandPoisoned = false;
                    hilSimulatorCommandPoisonReason = "";
                    hilSimulatorNavigationUnreadySinceMs = 0L;
                    hilSimulatorGroundRecoveryInFlight = false;
                    appendLog(newlyConnected
                            ? "HIL Simulator command fuse reset on new DJI product connection"
                            : "HIL Simulator command fuse reset on fresh Simulator session");
                }
                submitHilSimulatorSample(aircraftBridge.currentSimulatorSample());
                ensureHilSimulatorStarted();
                if (surveyAutoTakeoffPending && snapshot.getSticksActive()) {
                    cancelSurveyAutoTakeoff(getString(R.string.reason_rc_stick_takeover));
                }
                lastTelemetryAtElapsedMs = SystemClock.elapsedRealtime();
                Mini2OpenFlyRuntime runtime = localOpenFlyRuntime;
                if (runtime != null) runtime.updateTelemetry(snapshot);
                renderAircraftSnapshot(snapshot);
                monitorSurveyExternalIntervention(snapshot);
                monitorSurveyLowBatteryReturn();
                publishUeTelemetryIfEnabled(snapshot);
            }

            @Override public void onAction(String label, boolean ok, String message) {
                if (activityDestroyed) return;
                appendLog("DJI " + label + " " + (ok ? "OK" : "FAIL") + " · " + message);
                showBanner(label + "：" + message);
                boolean commandSendFailed = !ok && getString(R.string.control_send).equals(label);
                boolean commandLeaseExpired = getString(
                        R.string.virtual_stick_command_lease_expired).equals(label);
                if (commandSendFailed || commandLeaseExpired) {
                    String reason = commandLeaseExpired
                            ? getString(R.string.virtual_stick_command_lease_expired)
                            : getString(R.string.virtual_stick_send_failed, message);
                    if (isSurveySimulatorExecutionActive()) {
                        pauseSurveyForRecoverableFault(reason, true);
                    } else if (controlArmed || autoInferenceEnabled || inferenceInFlight) {
                        normalStop(reason);
                    }
                }
                if ("Virtual Stick".equals(label) && !ok) {
                    controlArmed = false;
                    if (surveySimulatorExecution != null
                            && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.ARMING) {
                        pauseSurveyForRecoverableFault(
                                getString(R.string.reason_virtual_stick_enable_failed, message), false);
                    }
                }
                if (SurveyRuntimeFaultPolicy.shouldPauseCameraAction(
                        label, ok, message, getString(R.string.survey_camera))) {
                    pauseSurveyForRecoverableFault(
                            getString(R.string.reason_survey_camera_command_timeout, message), true);
                }
                renderSafetyState();
            }

            @Override public void onManualTakeover() {
                if (activityDestroyed) return;
                appendLog("SAFETY RC stick takeover detected");
                boolean surveyWasActive = isSurveySimulatorExecutionActive();
                if (surveyWasActive) {
                    pauseSurveyForExternalIntervention(getString(R.string.reason_rc_stick_takeover), true);
                } else {
                    aircraftBridge.disableVirtualStick(getString(R.string.reason_rc_stick_takeover));
                }
                controlArmed = false;
                autoInferenceEnabled = false;
                mainHandler.removeCallbacks(autoInferenceRunnable);
                ((Button) findViewById(R.id.auto_infer_button)).setText(R.string.vln_auto_off_fullwidth);
                ((Button) findViewById(R.id.control_toggle_button)).setText(R.string.vln_control_off_fullwidth);
                showBanner(getString(R.string.vln_controller_override_released));
            }

            @Override public void onSimulatorSample(Mini2AircraftBridge.SimulatorSample sample) {
                if (activityDestroyed) return;
                submitHilSimulatorSample(sample);
            }
        }, this);
        aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
        aircraftBridge.bindCurrentProduct();
    }

    private void submitHilSimulatorSample(Mini2AircraftBridge.SimulatorSample sample) {
        submitHilSimulatorSample(sample, false);
    }

    private void submitHilSimulatorSample(
            Mini2AircraftBridge.SimulatorSample sample,
            boolean safeSessionSeed) {
        if (sample == null) return;
        AndroidHilController controller = hilController;
        if (controller == null || !controller.isRunning()) return;
        long rawAgeNanos = SystemClock.elapsedRealtimeNanos() - sample.getElapsedRealtimeNanos();
        boolean moving = sample.getMotorsOn() || sample.getFlying();
        if (moving && (rawAgeNanos < 0L
                || rawAgeNanos > edu.playground.djivln.hil.HilPoseFreshnessPolicy.MOVING_MAXIMUM_AGE_NANOS)) {
            controller.clearPose();
            if (!hilMovingRawStaleLatched) {
                hilMovingRawStaleLatched = true;
                String reason = getString(R.string.hil_simulator_raw_stale);
                appendLog(getString(R.string.hil_pose_stale_link_held, reason));
                if (isSurveySimulatorExecutionActive()) {
                    pauseSurveyForRecoverableFault(reason, true);
                } else if (hasActiveHilControlledOperation()) {
                    normalStop(reason);
                }
                showBanner(getString(R.string.control_paused_manual_resume, reason));
            }
            return;
        }
        if (hilMovingRawStaleLatched) {
            hilMovingRawStaleLatched = false;
            appendLog(getString(R.string.hil_simulator_raw_recovered_log));
            showBanner(getString(R.string.hil_simulator_raw_recovered));
        }
        try {
                Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
                int flags = 0;
                if (sample.getMotorsOn()) flags |= HilProtocol.POSE_FLAG_MOTORS_ON;
                if (sample.getFlying()) flags |= HilProtocol.POSE_FLAG_FLYING;
                if (!safeSessionSeed && snapshot.getVirtualStickEnabled()) {
                    flags |= HilProtocol.POSE_FLAG_VIRTUAL_STICK;
                }
                long age = snapshot.getFlightStateUpdatedAtMs() <= 0L ? Integer.MAX_VALUE
                        : Math.max(0L, System.currentTimeMillis() - snapshot.getFlightStateUpdatedAtMs());
                controller.submitPose(new HilProtocol.Pose(
                        sample.getElapsedRealtimeNanos(),
                        sample.getOriginLatitude(), sample.getOriginLongitude(),
                        sample.getEastMeters(), sample.getNorthMeters(), -sample.getDownMeters(),
                        sample.getRollDegrees(), sample.getPitchDegrees(), sample.getYawDegrees(),
                        safeSessionSeed ? 0.0 : snapshot.getVelocityNorth(),
                        safeSessionSeed ? 0.0 : snapshot.getVelocityEast(),
                        safeSessionSeed ? 0.0 : snapshot.getVerticalSpeed(),
                        snapshot.getGimbalPitch(),
                        safeSessionSeed ? 0.0 : sample.getCommandForward(),
                        safeSessionSeed ? 0.0 : sample.getCommandRight(),
                        safeSessionSeed ? 0.0 : sample.getCommandUp(),
                        safeSessionSeed ? 0.0 : sample.getCommandYawRate(),
                        (int) Math.min(Integer.MAX_VALUE, age),
                        (float) sample.getMeasuredRateHz(), flags));
        } catch (Throwable error) {
            Log.e(TAG, "HIL SimulatorState submit failed", error);
            appendLog("HIL SimulatorState submit failed: " + error);
        }
    }

    private void initIosPromptPresets() {
        loadPromptPresets();
        Spinner spinner = findViewById(R.id.prompt_preset_spinner);
        String[] labels = new String[promptPresets.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = promptPresets.get(i)[0];
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_prompt_spinner, labels) {
            private View style(View view) {
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextColor(getColor(R.color.text_link_blue));
                    text.setTextSize(11f);
                    text.setGravity(Gravity.CENTER);
                    text.setPadding(dp(4), 0, dp(4), 0);
                }
                return view;
            }
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                return style(super.getView(position, convertView, parent));
            }
            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getColor(R.color.text_primary));
                    ((TextView) view).setTextSize(12f);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.item_prompt_spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] preset = promptPresets.get(position);
                modelPrompt.setText(preset[1]);
                modelPrompt.setSelection(0);
                appendLog(getString(R.string.prompt_preset_selected, preset[0]));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinner.setSelection(0, false);
        modelPrompt.setText(promptPresets.get(0)[1]);
        modelPrompt.setSelection(0);
        appendLog(getString(R.string.prompt_ios_presets_loaded, promptPresets.size()));
    }

    private void loadPromptPresets() {
        promptPresets.clear();
        try (InputStream input = getAssets().open("prompt_presets.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8_192];
            int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            JSONObject document = new JSONObject(output.toString(StandardCharsets.UTF_8.name()));
            JSONArray items = document.getJSONArray("items");
            Set<String> identifiers = new HashSet<>();
            for (int index = 0; index < items.length(); index++) {
                JSONObject item = items.getJSONObject(index);
                String label = item.optString("label", "").trim();
                if (label.isEmpty()) label = String.format(Locale.US, "%03d", item.optInt("sample_idx", index));
                String instruction = item.getString("instruction").trim();
                if (instruction.isEmpty() || !identifiers.add(label)) {
                    throw new IllegalStateException("invalid or duplicate prompt " + label);
                }
                promptPresets.add(new String[] {label, instruction});
            }
            if (promptPresets.size() != EXPECTED_PROMPT_PRESET_COUNT) {
                throw new IllegalStateException("expected " + EXPECTED_PROMPT_PRESET_COUNT
                        + " prompts, got " + promptPresets.size());
            }
        } catch (Exception error) {
            throw new IllegalStateException(getString(R.string.uavflow_prompt_presets_load_failed), error);
        }
    }

    private void toggleVlnPanel() {
        vlnPanelMinimized = !vlnPanelMinimized;
        ((Mini2Application) getApplication()).setVlnPanelMinimized(vlnPanelMinimized);
        applyPanelMinimizedState();
    }

    private void applyPanelMinimizedState() {
        findViewById(R.id.vln_body).setVisibility(vlnPanelMinimized ? View.GONE : View.VISIBLE);
        findViewById(R.id.model_monitor_button).setVisibility(vlnPanelMinimized ? View.GONE : View.VISIBLE);
        findViewById(R.id.advanced_toggle_button).setVisibility(vlnPanelMinimized ? View.GONE : View.VISIBLE);
        ((Button) findViewById(R.id.vln_minimize_button)).setText(vlnPanelMinimized ? "+" : "−");
        findViewById(R.id.vln_minimize_button).setContentDescription(
                vlnPanelMinimized ? getString(R.string.expand_vln_panel) : getString(R.string.minimize_vln_panel));
        View panel = findViewById(R.id.vln_panel);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) panel.getLayoutParams();
        params.width = dp(vlnPanelMinimized ? 170 : expandedVlnWidthDp());
        panel.setLayoutParams(params);

        findViewById(R.id.flight_button_row).setVisibility(flightPanelMinimized ? View.GONE : View.VISIBLE);
        ((Button) findViewById(R.id.flight_panel_minimize_button)).setText(flightPanelMinimized ? "+" : "−");
        findViewById(R.id.flight_panel_minimize_button).setContentDescription(
                flightPanelMinimized ? getString(R.string.expand_flight_control_bar)
                        : getString(R.string.minimize_flight_control_bar));
    }

    private void toggleFlightPanel() {
        flightPanelMinimized = !flightPanelMinimized;
        ((Mini2Application) getApplication()).setFlightPanelMinimized(flightPanelMinimized);
        applyPanelMinimizedState();
    }

    private int expandedVlnWidthDp() {
        float widthDp = getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density;
        return widthDp < 820f ? 250 : 280;
    }

    private boolean isCompactHudWidth() {
        float widthDp = getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density;
        return widthDp < 820f;
    }

    private void applyResponsiveHudLayout() {
        boolean compact = isCompactHudWidth();
        View navigation = findViewById(R.id.navigation_hud);
        FrameLayout.LayoutParams navigationParams = (FrameLayout.LayoutParams) navigation.getLayoutParams();
        navigationParams.width = dp(compact ? 195 : 280);
        navigation.setLayoutParams(navigationParams);
        findViewById(R.id.location_source_text).setVisibility(compact ? View.GONE : View.VISIBLE);
        ((TextView) findViewById(R.id.monitor_title)).setText(compact
                ? R.string.logs : R.string.runtime_logs);

        View vln = findViewById(R.id.vln_panel);
        FrameLayout.LayoutParams vlnParams = (FrameLayout.LayoutParams) vln.getLayoutParams();
        vlnParams.width = dp(vlnPanelMinimized ? 170 : expandedVlnWidthDp());
        vln.setLayoutParams(vlnParams);
        updateMonitorAndFlightPanelLayout();
    }

    private void updateMonitorAndFlightPanelLayout() {
        View monitor = findViewById(R.id.model_monitor_panel);
        View flightPanel = findViewById(R.id.flight_control_panel);
        boolean monitorVisible = monitor.getVisibility() == View.VISIBLE;

        FrameLayout.LayoutParams monitorParams = (FrameLayout.LayoutParams) monitor.getLayoutParams();
        monitorParams.width = dp(isCompactHudWidth() ? 195 : 320);
        monitor.setLayoutParams(monitorParams);

        FrameLayout.LayoutParams flightParams = (FrameLayout.LayoutParams) flightPanel.getLayoutParams();
        if (monitorVisible) {
            flightParams.gravity = Gravity.TOP | Gravity.END;
            flightParams.rightMargin = dp(90);
        } else {
            flightParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            flightParams.rightMargin = 0;
        }
        flightPanel.setLayoutParams(flightParams);
    }

    private void confirmDangerousAction(String title, String message, Runnable action) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> action.run())
                .show();
    }

    private void renderAircraftSnapshot(Mini2AircraftBridge.Snapshot s) {
        recordMemorySnapshotIfDue();
        ((Button) findViewById(R.id.aircraft_status_button)).setText(s.getConnected()
                ? "DJI Aircraft" : getString(R.string.aircraft_disconnected));
        flightModeText.setText(s.getMode() + " · " + (s.getFlying() ? "FLY" : "GROUND"));
        gpsStatusText.setText("GPS " + s.getSatellites() + "\n" + s.getGpsLevel());
        rcStatusText.setText(s.getRcSignal() >= 0 ? s.getRcSignal() + "%\n"
                + compactRcMode(s.getRcMode()) : "--\n" + compactRcMode(s.getRcMode()));
        rcSignalView.setLevel(s.getRcSignal());
        aircraftBatteryText.setText(s.getAircraftBattery() >= 0
                ? formatDuration(s.getRemainingFlightTimeSeconds()) : "BAT --");
        ((BatteryRingView) findViewById(R.id.battery_ring)).setLevel(s.getAircraftBattery());
        ((FlightEnduranceBarView) findViewById(R.id.flight_endurance_bar)).render(s);
        rcBatteryText.setLevel(s.getRcBattery());
        compassText.setText(String.format(Locale.US, "N\nHDG %.0f°", s.getHeading()));
        speedText.setText(String.format(Locale.US, "H.S %.1f\nV.S %.1f", s.getHorizontalSpeed(), s.getVerticalSpeed()));
        String relativeHeight = Double.isFinite(s.getAltitude())
                ? String.format(Locale.US, "%.1f", s.getAltitude()) : "--";
        String groundClearance = Double.isFinite(s.getGroundClearance())
                ? String.format(Locale.US, "%.1f", s.getGroundClearance()) : "--";
        String estimatedAsl = Double.isFinite(s.getAsl())
                ? String.format(Locale.US, "%.1f", s.getAsl()) : "--";
        altitudeText.setText("H/B " + relativeHeight + "m\nAGL " + groundClearance
                + "m\nASL~ " + estimatedAsl + "m");
        altitudeText.setContentDescription(getString(R.string.altitude_accessibility,
                relativeHeight, groundClearance, estimatedAsl,
                getString(s.getDownwardVisionActive() ? R.string.vision_active : R.string.vision_inactive)));
        double homeDistance = s.getHomeDistanceMeters();
        distanceText.setText(String.format(Locale.US, "D %sm\nGIM %.0f°",
                Double.isFinite(homeDistance) ? String.format(Locale.US, "%.1f", homeDistance) : "--", s.getGimbalPitch()));
        TextView rthCurrent = findViewById(R.id.survey_rth_height_current);
        if (rthCurrent != null) {
            rthCurrent.setText(s.getGoHomeHeightMeters() > 0
                    ? getString(R.string.flight_controller_current_height, s.getGoHomeHeightMeters())
                    : getString(R.string.flight_controller_current_unavailable));
        }
        if (s.getRcLocationValid()) {
            locationSourceText.setText(String.format(Locale.US, "RC GPS\n%.3f\n%.3f", s.getRcLatitude(), s.getRcLongitude()));
        } else if (Double.isFinite(s.getLatitude()) && Double.isFinite(s.getLongitude())) {
            locationSourceText.setText(String.format(Locale.US, "AC GPS\n%.3f\n%.3f", s.getLatitude(), s.getLongitude()));
        } else {
            locationSourceText.setText(R.string.gps_position_placeholder);
        }
        cameraModeText.setText(s.getRecording() ? "● REC" : s.getCameraMode());
        recordingTimeText.setText(formatDuration(s.getRecordingSeconds()));
        findViewById(R.id.record_button).setContentDescription(getString(s.getRecording()
                ? R.string.recording_stop_description : R.string.recording_start_description));
        Button simulatorToggle = findViewById(R.id.simulator_toggle_button);
        simulatorToggle.setText(s.getSimulatorActive() ? R.string.simulator_on : R.string.simulator_off);
        simulatorToggle.setEnabled(s.getConnected() && s.getSimulatorAvailable());
        TextView simulatorStatus = findViewById(R.id.simulator_status_text);
        if (!s.getSimulatorAvailable()) {
            simulatorStatus.setText(R.string.simulator_capability_unavailable);
        } else if (s.getSimulatorActive()) {
            simulatorStatus.setText(String.format(Locale.US,
                    "E %.1f  N %.1f  D %.1f m · R/P/Y %.1f/%.1f/%.1f° · %.1f Hz · %s",
                    s.getSimulatorX(), s.getSimulatorY(), s.getSimulatorZ(),
                    s.getSimulatorRoll(), s.getSimulatorPitch(), s.getSimulatorYaw(),
                    s.getSimulatorStateHz(),
                    getString(s.getSimulatorFlying() ? R.string.simulator_flying : R.string.simulator_ground)));
        } else {
            simulatorStatus.setText(getString(R.string.simulator_available_off_rate,
                    hilSimulatorStateHz));
        }
        if (surveySimulatorExecution != null
                && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.PAUSED) {
            simulatorStatus.append("\n" + getString(
                    R.string.simulator_switch_paused_checkpoint_preserved));
        }
        Button phoneCharging = findViewById(R.id.rc_phone_charging_button);
        TextView phoneChargingStatus = findViewById(R.id.rc_phone_charging_status);
        boolean chargingAvailable = s.getConnected() && s.getRcPhoneChargingAvailable();
        phoneCharging.setEnabled(chargingAvailable);
        if (!chargingAvailable) {
            phoneCharging.setText(R.string.rc_phone_charging_unavailable);
            phoneChargingStatus.setText(R.string.rc_phone_charging_unavailable_hint);
        } else {
            boolean charging = "ALWAYS".equals(s.getRcPhoneChargingMode());
            phoneCharging.setText(charging
                    ? R.string.rc_phone_charging_on : R.string.rc_phone_charging_off);
            phoneChargingStatus.setText(getString(R.string.rc_phone_charging_mode,
                    s.getRcPhoneChargingMode()));
        }
        findViewById(R.id.confirm_land_button).setVisibility(s.getLandingConfirmationNeeded() ? View.VISIBLE : View.GONE);
        boolean connected = s.getConnected();
        boolean stableFlightActionState = !s.getGoingHome() && !s.getLanding();
        setControlAvailability(R.id.takeoff_button,
                connected && !s.getFlying() && stableFlightActionState);
        setControlAvailability(R.id.rth_button,
                connected && s.getFlying() && stableFlightActionState);
        setControlAvailability(R.id.cancel_rth_button, connected && s.getGoingHome());
        setControlAvailability(R.id.land_button,
                connected && s.getFlying() && !s.getLanding());
        setControlAvailability(R.id.cancel_land_button, connected && s.getLanding());
        setControlAvailability(R.id.confirm_land_button,
                connected && s.getLandingConfirmationNeeded());
        setControlAvailability(R.id.shutter_button, connected && !s.getRecording());
        setControlAvailability(R.id.record_button, connected);
        setControlAvailability(R.id.gallery_button, connected && !s.getFlying());
        findViewById(R.id.enable_vs_button).setEnabled(s.getConnected());
        updateMap(s);
        renderSafetyState();
    }

    private void recordMemorySnapshotIfDue() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastMemoryLogElapsedMs < 30_000L) return;
        lastMemoryLogElapsedMs = now;
        Runtime runtime = Runtime.getRuntime();
        Debug.MemoryInfo memory = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memory);
        String message = String.format(Locale.US,
                "MEMORY javaUsed=%d javaHeap=%d javaMax=%d native=%d totalPssKb=%d privateDirtyKb=%d",
                runtime.totalMemory() - runtime.freeMemory(), runtime.totalMemory(),
                runtime.maxMemory(), Debug.getNativeHeapAllocatedSize(), memory.getTotalPss(),
                memory.getTotalPrivateDirty());
        Log.i(TAG, message);
        persistLogLine(String.format(Locale.US, "%tT.%<tL  %s\n",
                System.currentTimeMillis(), message));
    }

    private void setControlAvailability(int id, boolean enabled) {
        View view = findViewById(id);
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.35f);
    }

    private void setSurveyGoHomeHeight() {
        EditText input = findViewById(R.id.survey_rth_height_input);
        if (input == null || aircraftBridge == null) return;
        int height;
        try {
            height = Integer.parseInt(input.getText().toString().trim());
        } catch (RuntimeException error) {
            input.setError(getString(R.string.rth_height_integer_error));
            return;
        }
        if (height < 20 || height > 500) {
            input.setError(getString(R.string.rth_height_range_error));
            return;
        }
        if (!aircraftSnapshot.getConnected()) {
            input.setError(getString(R.string.flight_controller_disconnected));
            return;
        }
        if (aircraftSnapshot.getFlying()) {
            input.setError(getString(R.string.rth_height_ground_only));
            return;
        }
        double missionMaximum = surveyMission == null
                ? Double.NaN : surveyMission.getWaypoints().stream()
                .mapToDouble(waypoint -> waypoint.getPoint().getAltitudeMeters()).max()
                .orElse(surveyMission.getConstraints().getSafeTakeoffAltitudeMeters());
        if (Double.isFinite(missionMaximum) && height + 0.5 < missionMaximum) {
            input.setError(getString(R.string.rth_height_below_mission, missionMaximum));
            return;
        }
        input.setError(null);
        appendLog("SURVEY set RTH height requested=" + height + "m · aircraft grounded");
        aircraftBridge.setGoHomeHeightMeters(height, (ok, message) -> runOnUiThread(() -> {
            if (ok) {
                input.setText(String.valueOf(height));
                appendLog("SURVEY set RTH height OK=" + height + "m");
        showBanner(getString(R.string.rth_height_written, height));
            } else {
                input.setError(getString(R.string.write_failed, message));
                appendLog("SURVEY set RTH height FAIL=" + height + "m · " + message);
            }
        }));
    }

    private String formatDuration(int totalSeconds) {
        int safe = Math.max(0, totalSeconds);
        return String.format(Locale.US, "%02d:%02d", safe / 60, safe % 60);
    }

    private String compactRcMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) return "--";
        String normalized = mode.trim().toUpperCase(Locale.US);
        if (normalized.startsWith("POSITION_")) return "P" + normalized
                .substring("POSITION_".length())
                .replace("ONE", "1").replace("TWO", "2").replace("THREE", "3");
        if (normalized.equals("NORMAL")) return "N";
        if (normalized.equals("SPORT")) return "S";
        if (normalized.equals("TRIPOD")) return "T";
        if (normalized.equals("FUNCTION")) return "F";
        return normalized.length() <= 3 ? normalized : normalized.substring(0, 3);
    }

    private void renderSafetyState() {
        long now = SystemClock.elapsedRealtime();
        boolean recentFrame = packetCount > 0 && now - lastFrameAtElapsedMs <= 2_000L;
        boolean freshTelemetry = aircraftSnapshot.getConnected() && now - lastTelemetryAtElapsedMs <= 2_000L;
        String reason;
        if (emergencyStopped) reason = getString(R.string.emergency_stop_locked);
        else if (!aircraftSnapshot.getConnected()) reason = getString(R.string.aircraft_disconnected);
        else if (!recentFrame) reason = getString(R.string.no_recent_camera_frame);
        else if (!freshTelemetry) reason = getString(R.string.gate_telemetry_stale);
        else if (!aircraftSnapshot.getVirtualStickEnabled()) reason = getString(R.string.virtual_stick_not_enabled_short);
        else if (!controlArmed) reason = getString(R.string.control_not_authorized);
        else reason = "OK";
        boolean eligible = "OK".equals(reason);
        safetyGateText.setText(eligible ? "SAFE GATE" : "SAFE HOLD");
        safetyGateText.setTextColor(eligible ? getColor(R.color.text_ok_green) : getColor(R.color.text_accent_amber));

        String mode;
        String owner;
        if (!aircraftSnapshot.getConnected() || emergencyStopped) {
            mode = getString(R.string.flight_mode_emergency_or_disconnect);
            owner = getString(R.string.control_owner_none);
        } else if (aircraftSnapshot.getLanding()) {
            mode = getString(R.string.flight_mode_auto_landing);
            owner = getString(R.string.control_owner_flight_controller);
        } else if (aircraftSnapshot.getGoingHome()) {
            mode = getString(R.string.flight_mode_auto_rth);
            owner = getString(R.string.control_owner_flight_controller);
        } else if (!controlArmed) {
            mode = getString(R.string.flight_mode_manual_control);
            owner = getString(R.string.control_owner_remote_controller);
        } else {
            mode = getString(R.string.flight_mode_vln_standby);
            owner = "VLN";
        }
        AndroidHilController.Status hilStatus = hilController == null
                ? null : hilController.latestStatus();
        boolean hilRunning = hilController != null && hilController.isRunning();
        String environment;
        if (hilRunning) {
            environment = hilStatus != null && hilStatus.getPeerFresh()
                    ? getString(R.string.environment_hil_ue_online)
                    : getString(R.string.environment_hil_waiting_for_ue);
        } else if (aircraftSnapshot.getSimulatorActive()) {
            environment = getString(R.string.environment_dji_simulator);
        } else {
            environment = getString(R.string.environment_real_aircraft);
        }
        flightStatusText.setText(getString(
                R.string.safety_owner_status, mode, owner, reason, environment));
        ((Button) findViewById(R.id.control_toggle_button)).setText(controlArmed
                ? R.string.vln_control_on : R.string.vln_control_off);
        ((Button) findViewById(R.id.emergency_stop_button)).setText(emergencyStopped
                ? R.string.emergency_stopped : R.string.emergency_stop);
        boolean canInfer = modelLoaded && recentFrame;
        boolean canStartInference = canInfer && (!continuousChunkEnabled || controlArmed);
        boolean canArm = canInfer && aircraftSnapshot.getConnected() && aircraftSnapshot.getFlying() && !emergencyStopped;
        View inferButton = findViewById(R.id.model_infer_once);
        inferButton.setEnabled(canStartInference && !inferenceInFlight);
        inferButton.setAlpha(inferButton.isEnabled() ? 1f : 0.35f);
        View autoButton = findViewById(R.id.auto_infer_button);
        autoButton.setEnabled(autoInferenceEnabled || canStartInference);
        autoButton.setAlpha(autoButton.isEnabled() ? 1f : 0.35f);
        View controlButton = findViewById(R.id.control_toggle_button);
        controlButton.setEnabled(controlArmed || canArm);
        controlButton.setAlpha(controlButton.isEnabled() ? 1f : 0.35f);
        boolean canExecuteManual = aircraftSnapshot.getConnected()
                && aircraftSnapshot.getFlying() && !emergencyStopped && freshTelemetry;
        View manualButton = findViewById(R.id.manual_xyz_button);
        manualButton.setEnabled(canExecuteManual);
        manualButton.setAlpha(canExecuteManual ? 1f : 0.35f);
        View manualStopButton = findViewById(R.id.manual_xyz_stop_button);
        manualStopButton.setEnabled(controlArmed);
        manualStopButton.setAlpha(controlArmed ? 1f : 0.35f);
        advancedStateText.setText(getString(R.string.safety_gate_summary,
                reason,
                getString(aircraftSnapshot.getVirtualStickEnabled() ? R.string.state_on : R.string.state_off),
                getString(controlArmed ? R.string.state_on : R.string.state_off),
                getString(aircraftSnapshot.getSticksActive() ? R.string.state_stick_active : R.string.state_none),
                getString(velocityEstimateMode ? R.string.velocity_estimate : R.string.gps_loop),
                speedLimitMetersPerSecond, executedPrefix,
                getString(flyThroughEnabled ? R.string.state_on : R.string.state_off)));
    }

    private void renderChunkConfiguration() {
        Button modeButton = findViewById(R.id.chunk_mode_button);
        modeButton.setText(continuousChunkEnabled ? R.string.uavflow_on : R.string.uavflow_off);
        TextView help = findViewById(R.id.chunk_help_text);
        help.setText(getString(R.string.vln_chunk_execution_value_hint, executedPrefix));
    }

    private String coreSafetyIssue() {
        long now = SystemClock.elapsedRealtime();
        if (emergencyStopped) return getString(R.string.emergency_lock_not_cleared);
        if (!aircraftSnapshot.getConnected()) return getString(R.string.flight_controller_disconnected);
        if (packetCount == 0 || now - lastFrameAtElapsedMs > 2_000L) return getString(R.string.dji_camera_frame_not_ready);
        if (now - lastTelemetryAtElapsedMs > 2_000L) return getString(R.string.gate_telemetry_stale);
        return null;
    }

    private void toggleAutoInference(View source) {
        if (!autoInferenceEnabled) {
            if (isSurveySimulatorControlReserved()) {
                showBanner(getString(R.string.vln_auto_blocked_survey_simulator));
                return;
            }
            String issue = canInferIssue();
            if (issue != null) {
                showBanner(issue);
                appendLog("MODEL auto inference blocked: " + issue);
                return;
            }
            if (continuousChunkEnabled && !controlArmed) {
                showBanner(getString(R.string.vln_enable_control_before_auto, executedPrefix));
                return;
            }
        }
        autoInferenceEnabled = !autoInferenceEnabled;
        ((Button) source).setText(autoInferenceEnabled ? R.string.vln_auto_on : R.string.vln_auto_off);
        mainHandler.removeCallbacks(autoInferenceRunnable);
        if (autoInferenceEnabled) {
            appendLog("MODEL automatic inference enabled");
            mainHandler.post(autoInferenceRunnable);
        } else {
            appendLog("MODEL automatic inference disabled");
        }
    }

    private void toggleFlightControl(View source) {
        if (isSurveySimulatorControlReserved()) {
            showBanner(getString(R.string.survey_vs_occupied_pause_or_abort));
            return;
        }
        if (controlArmed) {
            controlArmed = false;
            lastYawRateDegreesPerSecond = 0.0;
            aircraftBridge.disableVirtualStick(getString(R.string.reason_user_disabled_vln_control));
            renderSafetyState();
            return;
        }
        String issue = coreSafetyIssue();
        if (issue == null && !aircraftSnapshot.getFlying()) {
            issue = getString(R.string.vln_enable_requires_flying);
        }
        if (issue == null && !modelLoaded) issue = getString(R.string.vln_enable_requires_model);
        if (issue == null && hilVirtualFramesEnabled) issue = canInferIssue();
        if (issue == null && !velocityEstimateMode
                && (!Double.isFinite(aircraftSnapshot.getLatitude())
                || !Double.isFinite(aircraftSnapshot.getLongitude()))) {
            issue = getString(R.string.gps_not_ready_velocity_estimate_available);
        }
        if (issue != null) {
            appendLog("SAFETY control blocked: " + issue);
            showBanner(getString(R.string.control_blocked_reason, issue));
            return;
        }
        controlArmed = true;
        localRuntime().resetEstimatedModelState();
        aircraftBridge.enableVirtualStick();
        appendLog("CONTROL arm requested");
        renderSafetyState();
    }

    private void activateEmergencyStop(String reason) {
        abortSurveySimulatorExecution(reason, false);
        emergencyStopped = true;
        controlArmed = false;
        lastYawRateDegreesPerSecond = 0.0;
        autoInferenceEnabled = false;
        inferenceInFlight = false;
        abortChunkExecution(getString(R.string.emergency_stop));
        mainHandler.removeCallbacks(autoInferenceRunnable);
        mainHandler.removeCallbacks(stopVelocityRunnable);
        ((Button) findViewById(R.id.auto_infer_button)).setText(getString(R.string.vln_auto_off));
        if (aircraftBridge != null) aircraftBridge.disableVirtualStick(reason);
        commandText.setText(R.string.vln_emergency_zero_released);
        appendLog("SAFETY emergency stop: " + reason);
        runModelOperation("stop");
        showBanner(getString(R.string.emergency_locked_zero_vs_released));
        renderSafetyState();
    }

    private String canInferIssue() {
        if (!modelLoaded) return getString(R.string.model_not_loaded);
        if (hilVirtualFramesEnabled) {
            if (hilController == null || !hilController.isRunning()) return getString(R.string.hil_link_not_started);
            if (!hilController.isPeerFresh()) return getString(R.string.hil_heartbeat_not_ready);
            edu.playground.djivln.hil.HilVirtualFrameStore.Snapshot frame = hilController.frameSnapshot();
            if (frame == null || frame.getAgeMillis() > 2_000L) return getString(R.string.hil_virtual_frame_not_ready);
            return null;
        }
        long now = SystemClock.elapsedRealtime();
        if (packetCount == 0 || now - lastFrameAtElapsedMs > 2_000L) return getString(R.string.dji_camera_frame_not_ready);
        return null;
    }

    private void normalStop(String reason) {
        stopVlnControl(reason, true);
    }

    private void stopVlnControl(String reason, boolean abortSurvey) {
        if (abortSurvey) abortSurveySimulatorExecution(reason, false);
        autoInferenceEnabled = false;
        controlArmed = false;
        lastYawRateDegreesPerSecond = 0.0;
        abortChunkExecution(reason);
        mainHandler.removeCallbacks(autoInferenceRunnable);
        mainHandler.removeCallbacks(stopVelocityRunnable);
        ((Button) findViewById(R.id.auto_infer_button)).setText(getString(R.string.vln_auto_off));
        if (aircraftBridge != null) aircraftBridge.disableVirtualStick(reason);
        commandText.setText(R.string.vln_idle_command);
        appendLog(getString(R.string.control_stopped_reason, reason));
        runModelOperation("stop");
        renderSafetyState();
    }

    private void resetEmergency() {
        abortSurveySimulatorExecution(getString(R.string.reason_emergency_stop_reset), false);
        emergencyStopped = false;
        autoInferenceEnabled = false;
        controlArmed = false;
        lastYawRateDegreesPerSecond = 0.0;
        abortChunkExecution(getString(R.string.reason_emergency_stop_reset));
        mainHandler.removeCallbacks(autoInferenceRunnable);
        mainHandler.removeCallbacks(stopVelocityRunnable);
        ((Button) findViewById(R.id.auto_infer_button)).setText(getString(R.string.vln_auto_off));
        if (aircraftBridge != null) {
            aircraftBridge.disableVirtualStick(getString(R.string.reason_emergency_reset_no_auto_restart));
        }
        commandText.setText(R.string.vln_idle_command);
        appendLog(getString(R.string.emergency_reset_manual_zero));
        runModelOperation("reset");
        renderSafetyState();
    }

    private void runManualXyz() {
        if (isSurveySimulatorControlReserved()) {
            showBanner(getString(R.string.survey_vs_occupied_manual_xyz));
            return;
        }
        double x;
        double y;
        double z;
        try {
            x = parseNumber((EditText) findViewById(R.id.manual_x));
            y = parseNumber((EditText) findViewById(R.id.manual_y));
            z = parseNumber((EditText) findViewById(R.id.manual_z));
        } catch (IllegalArgumentException error) {
            showBanner(getString(R.string.manual_xyz_invalid_number));
            return;
        }
        if (Math.hypot(x, y) > 10.0 || Math.abs(z) > 0.5) {
            showBanner(getString(R.string.manual_xyz_out_of_range));
            return;
        }
        String descentIssue = descentSafetyIssue(z);
        if (descentIssue != null) {
            showBanner(getString(R.string.manual_xyz_blocked_reason, descentIssue));
            return;
        }
        String issue = manualSafetyIssue();
        if (issue != null) {
            appendLog("SAFETY manual XYZ blocked: " + issue);
            showBanner(getString(R.string.manual_xyz_blocked_reason, issue));
            return;
        }
        final double targetX = x;
        final double targetY = y;
        final double targetZ = z;
        if (!aircraftSnapshot.getVirtualStickEnabled()) {
            showBanner(getString(R.string.manual_xyz_acquiring_vs));
            aircraftBridge.enableVirtualStickForManual(() -> {
                controlArmed = true;
                beginManualXyz(targetX, targetY, targetZ);
                renderSafetyState();
            });
            return;
        }
        controlArmed = true;
        beginManualXyz(targetX, targetY, targetZ);
        renderSafetyState();
    }

    private void beginManualXyz(double x, double y, double z) {
        commandText.setText(getString(R.string.manual_xyz_target,
                velocityEstimateMode ? getString(R.string.velocity_estimation)
                        : getString(R.string.gps_closed_loop), x, y, z));
        appendLog(String.format(Locale.US, "CONTROL manual XYZ start x=%.2f y=%.2f z=%.2f mode=%s", x, y, z,
                velocityEstimateMode ? "velocity" : "gps"));
        lastYawRateDegreesPerSecond = 0.0;
        double referenceHeading = Double.isFinite(aircraftSnapshot.getHeading())
                ? aircraftSnapshot.getHeading() : 0.0;
        if (velocityEstimateMode) startTimedRelativeMove(x, y, z, false, referenceHeading, 0.0);
        else startGpsRelativeMove(x, y, z, false, referenceHeading, 0.0);
    }

    private String manualSafetyIssue() {
        long now = SystemClock.elapsedRealtime();
        if (emergencyStopped) return getString(R.string.emergency_lock_not_cleared);
        if (!aircraftSnapshot.getConnected()) return getString(R.string.flight_controller_disconnected);
        if (!aircraftSnapshot.getFlying()) return getString(R.string.aircraft_not_flying);
        if (now - lastTelemetryAtElapsedMs > 2_000L) return "flight telemetry is missing or stale";
        return null;
    }

    private double parseNumber(EditText input) {
        String value = input.getText().toString().trim();
        if (value.isEmpty()) return 0.0;
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(error);
        }
    }

    /** Returns a human-readable block reason, or null when the descent is safe. */
    private String descentSafetyIssue(double upMeters) {
        if (upMeters >= -0.05) return null;

        final boolean hasReliableGroundClearance =
                aircraftSnapshot.getGroundClearanceReliableForSafety()
                        && Double.isFinite(aircraftSnapshot.getGroundClearance());
        final double currentHeight = hasReliableGroundClearance
                ? aircraftSnapshot.getGroundClearance()
                : aircraftSnapshot.getAltitude();
        if (!Double.isFinite(currentHeight)) {
            return getString(R.string.descent_height_unavailable);
        }
        if (currentHeight + upMeters < 0.8) {
            return hasReliableGroundClearance
                    ? getString(R.string.descent_target_below_downward_clearance)
                    : getString(R.string.descent_target_below_relative_height_no_rangefinder);
        }
        return null;
    }

    private void startTimedRelativeMove(
            double x,
            double y,
            double z,
            boolean alignYawToAction,
            double referenceHeadingDegrees,
            double yawDeltaDegrees) {
        Mini2AircraftBridge.Snapshot start = aircraftSnapshot;
        final double currentHeadingDegrees = Double.isFinite(start.getHeading()) ? start.getHeading() : 0.0;
        final double referenceYaw = Math.toRadians(referenceHeadingDegrees);
        final double targetNorth = flyThroughCarryNorthMeters
                + x * Math.cos(referenceYaw) - y * Math.sin(referenceYaw);
        final double targetEast = flyThroughCarryEastMeters
                + x * Math.sin(referenceYaw) + y * Math.cos(referenceYaw);
        final double targetUp = flyThroughCarryUpMeters + z;
        flyThroughCarryNorthMeters = 0.0;
        flyThroughCarryEastMeters = 0.0;
        flyThroughCarryUpMeters = 0.0;
        final double segmentHorizontal = Math.hypot(x, y);
        final double targetHeadingDegrees = alignYawToAction
                ? OrinTrajectorySemantics.wrapDegrees(referenceHeadingDegrees + yawDeltaDegrees)
                : currentHeadingDegrees;
        final boolean flyThroughThisStep = alignYawToAction && flyThroughEnabled
                && hasFollowingContinuousStep();
        final long timeoutMs = flyThroughThisStep
                ? OrinTrajectorySemantics.flyThroughTimeoutMillis(segmentHorizontal)
                : OrinTrajectorySemantics.finalPositionTimeoutMillis(segmentHorizontal, z);
        final long deadline = SystemClock.elapsedRealtime() + timeoutMs;
        final double horizontalTolerance = flyThroughThisStep
                ? OrinTrajectorySemantics.flyThroughRadiusMeters(segmentHorizontal)
                : 0.08;
        final double[] estimated = {0.0, 0.0, 0.0};
        final long[] lastTelemetryMs = {start.getFlightStateUpdatedAtMs()};
        Runnable controller = new Runnable() {
            @Override public void run() {
                if (activeRelativeMoveRunnable != this) return;
                String issue = manualSafetyIssue();
                if (issue != null || !controlArmed || emergencyStopped) {
                    String reason = issue == null ? getString(R.string.control_released) : issue;
                    appendLog(getString(R.string.position_action_aborted, reason));
                    finishRelativeMove(false, reason);
                    return;
                }
                Mini2AircraftBridge.Snapshot s = aircraftSnapshot;
                long timestamp = s.getFlightStateUpdatedAtMs();
                if (timestamp > lastTelemetryMs[0]) {
                    double dt = Math.min(0.25, (timestamp - lastTelemetryMs[0]) / 1_000.0);
                    estimated[0] += s.getVelocityNorth() * dt;
                    estimated[1] += s.getVelocityEast() * dt;
                    double measuredUp = Math.abs(s.getVerticalSpeed()) < 0.03 ? 0.0 : s.getVerticalSpeed();
                    estimated[2] += clamp(measuredUp, -2.0, 2.0) * dt;
                    lastTelemetryMs[0] = timestamp;
                }
                double northError = targetNorth - estimated[0];
                double eastError = targetEast - estimated[1];
                double verticalError = targetUp - estimated[2];
                double horizontalError = Math.hypot(northError, eastError);
                boolean verticalDone = Math.abs(verticalError) <= 0.10 && Math.abs(s.getVerticalSpeed()) <= 0.12;
                boolean reached = horizontalError <= horizontalTolerance && verticalDone;
                boolean timedOut = SystemClock.elapsedRealtime() >= deadline;
                if (reached || timedOut) {
                    double executedHorizontal = Math.hypot(estimated[0], estimated[1]);
                    appendLog(getString(
                            reached ? R.string.position_action_progress_complete
                                    : R.string.position_action_progress_timeout,
                            Math.hypot(targetNorth, targetEast), executedHorizontal, horizontalError));
                    if (reached && flyThroughThisStep) {
                        flyThroughCarryNorthMeters = northError;
                        flyThroughCarryEastMeters = eastError;
                        flyThroughCarryUpMeters = verticalError;
                    }
                    finishRelativeMove(reached, getString(reached
                            ? R.string.reason_position_action_complete
                            : R.string.reason_position_action_timeout));
                    return;
                }
                double yaw = Math.toRadians(s.getHeading());
                double forwardError = northError * Math.cos(yaw) + eastError * Math.sin(yaw);
                double rightError = -northError * Math.sin(yaw) + eastError * Math.cos(yaw);
                double forward = clamp(forwardError * 0.8, -speedLimitMetersPerSecond, speedLimitMetersPerSecond);
                double right = clamp(rightError * 0.8, -speedLimitMetersPerSecond, speedLimitMetersPerSecond);
                double norm = Math.hypot(forward, right);
                if (norm > speedLimitMetersPerSecond) {
                    forward *= speedLimitMetersPerSecond / norm;
                    right *= speedLimitMetersPerSecond / norm;
                }
                double[] continuousVelocity = ContinuousStepVelocity.preserveHorizontalMomentum(
                        forward, right, s.getHorizontalSpeed(), speedLimitMetersPerSecond,
                        flyThroughThisStep);
                forward = continuousVelocity[0];
                right = continuousVelocity[1];
                double up = Math.abs(verticalError) <= 0.10 ? 0.0 : clamp(verticalError * 0.7, -0.2, 0.2);
                if (up != 0.0 && Math.abs(up) < 0.12) up = Math.copySign(0.12, up);
                double yawRate = 0.0;
                if (alignYawToAction) {
                    yawRate = OrinTrajectorySemantics.yawRateDegreesPerSecond(
                            s.getHeading(), targetHeadingDegrees,
                            lastYawRateDegreesPerSecond, 0.1);
                    lastYawRateDegreesPerSecond = yawRate;
                }
                aircraftBridge.sendBodyVelocity(
                        (float) forward, (float) right, (float) up, (float) yawRate);
                mainHandler.postDelayed(this, VLN_CONTROL_INTERVAL_MS);
            }
        };
        activeRelativeMoveRunnable = controller;
        mainHandler.post(controller);
    }

    private void startGpsRelativeMove(
            double x,
            double y,
            double z,
            boolean alignYawToAction,
            double referenceHeadingDegrees,
            double yawDeltaDegrees) {
        Mini2AircraftBridge.Snapshot start = aircraftSnapshot;
        if (!Double.isFinite(start.getLatitude()) || !Double.isFinite(start.getLongitude())) {
            showBanner(getString(R.string.manual_xyz_gps_unavailable));
            return;
        }
        final double currentHeadingDegrees = Double.isFinite(start.getHeading()) ? start.getHeading() : 0.0;
        final double heading = Math.toRadians(referenceHeadingDegrees);
        final double north = flyThroughCarryNorthMeters
                + x * Math.cos(heading) - y * Math.sin(heading);
        final double east = flyThroughCarryEastMeters
                + x * Math.sin(heading) + y * Math.cos(heading);
        final double targetUp = flyThroughCarryUpMeters + z;
        flyThroughCarryNorthMeters = 0.0;
        flyThroughCarryEastMeters = 0.0;
        flyThroughCarryUpMeters = 0.0;
        final double targetLat = start.getLatitude() + north / 111_111.0;
        final double lonScale = Math.max(0.01, Math.cos(Math.toRadians(start.getLatitude())));
        final double targetLon = start.getLongitude() + east / (111_111.0 * lonScale);
        final double targetAlt = start.getAltitude() + targetUp;
        final double targetHorizontal = Math.hypot(x, y);
        final double targetHeadingDegrees = alignYawToAction
                ? OrinTrajectorySemantics.wrapDegrees(referenceHeadingDegrees + yawDeltaDegrees)
                : currentHeadingDegrees;
        final boolean flyThroughThisStep = alignYawToAction && flyThroughEnabled
                && hasFollowingContinuousStep();
        final long timeoutMs = flyThroughThisStep
                ? OrinTrajectorySemantics.flyThroughTimeoutMillis(targetHorizontal)
                : 15_000L;
        final long deadline = SystemClock.elapsedRealtime() + timeoutMs;
        final double horizontalTolerance = flyThroughThisStep
                ? Math.max(0.8, OrinTrajectorySemantics.flyThroughRadiusMeters(targetHorizontal))
                : 0.8;
        Runnable controller = new Runnable() {
            @Override public void run() {
                if (activeRelativeMoveRunnable != this) return;
                String issue = manualSafetyIssue();
                if (issue != null || !controlArmed || emergencyStopped) {
                    String reason = issue == null ? getString(R.string.control_released) : issue;
                    finishRelativeMove(false, reason);
                    appendLog("CONTROL GPS closure aborted: " + reason);
                    return;
                }
                Mini2AircraftBridge.Snapshot s = aircraftSnapshot;
                GeoPoint current = effectiveAircraftGeoPoint(s);
                if (current == null) {
                    finishRelativeMove(false, getString(R.string.gate_gps_unavailable));
                    return;
                }
                double northError = (targetLat - current.getLatitude()) * 111_111.0;
                double eastError = (targetLon - current.getLongitude()) * 111_111.0 *
                        Math.max(0.01, Math.cos(Math.toRadians(current.getLatitude())));
                double horizontalError = Math.hypot(northError, eastError);
                double verticalError = targetAlt - current.getAltitudeMeters();
                boolean reached = horizontalError <= horizontalTolerance && Math.abs(verticalError) <= 0.2;
                boolean timedOut = SystemClock.elapsedRealtime() >= deadline;
                if (reached || timedOut) {
                    appendLog(String.format(Locale.US,
                            reached ? "CONTROL GPS closure reached h=%.2fm z=%.2fm"
                                    : "CONTROL GPS closure timeout h=%.2fm z=%.2fm",
                            horizontalError, verticalError));
                    if (reached && flyThroughThisStep) {
                        flyThroughCarryNorthMeters = northError;
                        flyThroughCarryEastMeters = eastError;
                        flyThroughCarryUpMeters = verticalError;
                    }
                    finishRelativeMove(reached, getString(reached
                            ? R.string.reason_gps_position_action_complete
                            : R.string.reason_gps_position_action_timeout));
                    return;
                }
                double yaw = Math.toRadians(s.getHeading());
                double forwardError = northError * Math.cos(yaw) + eastError * Math.sin(yaw);
                double rightError = -northError * Math.sin(yaw) + eastError * Math.cos(yaw);
                double effectiveSpeedLimit = speedLimitMetersPerSecond;
                double forward = clamp(forwardError * 0.6, -effectiveSpeedLimit, effectiveSpeedLimit);
                double right = clamp(rightError * 0.6, -effectiveSpeedLimit, effectiveSpeedLimit);
                double norm = Math.hypot(forward, right);
                if (norm > effectiveSpeedLimit) {
                    forward *= effectiveSpeedLimit / norm;
                    right *= effectiveSpeedLimit / norm;
                }
                double[] continuousVelocity = ContinuousStepVelocity.preserveHorizontalMomentum(
                        forward, right, s.getHorizontalSpeed(), effectiveSpeedLimit,
                        flyThroughThisStep);
                forward = continuousVelocity[0];
                right = continuousVelocity[1];
                float up = (float) clamp(verticalError * 0.5, -0.2, 0.2);
                double yawRate = 0.0;
                if (alignYawToAction) {
                    yawRate = OrinTrajectorySemantics.yawRateDegreesPerSecond(
                            s.getHeading(), targetHeadingDegrees,
                            lastYawRateDegreesPerSecond, VLN_CONTROL_INTERVAL_SECONDS);
                    lastYawRateDegreesPerSecond = yawRate;
                }
                aircraftBridge.sendBodyVelocity((float) forward, (float) right, up, (float) yawRate);
                mainHandler.postDelayed(this, VLN_CONTROL_INTERVAL_MS);
            }
        };
        activeRelativeMoveRunnable = controller;
        mainHandler.post(controller);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateMap(Mini2AircraftBridge.Snapshot snapshot) {
        if (amap == null) return;
        SurveySimulatorMapPose aircraftMapPose = effectiveAircraftMapPose(snapshot);
        GeoPoint aircraftPoint = aircraftMapPose == null ? null : aircraftMapPose.getPoint();
        if (aircraftPoint != null) {
            double aircraftMapHeading = aircraftMapPose.getHeadingDegrees();
            GeoPoint displayPoint = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(
                    aircraftPoint);
            LatLng position = new LatLng(displayPoint.getLatitude(), displayPoint.getLongitude());
            if (aircraftMarker == null) {
                aircraftMarker = (Marker) amap.addOverlay(new MarkerOptions()
                        .position(position)
                        .title(getString(R.string.map_aircraft_title, snapshot.getProduct()))
                        .icon(aircraftMapIcon())
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .zIndex(12));
                lastAircraftMarkerPoint = aircraftPoint;
                lastAircraftMarkerHeading = aircraftMapHeading;
                lastAircraftMarkerUpdateMs = SystemClock.elapsedRealtime();
                aircraftMarkerVisible = true;
                aircraftMarker.setRotate(
                        FlightMapPresentation.mapMarkerRotationDegrees(aircraftMapHeading));
            } else if (MapMarkerUpdatePolicy.INSTANCE.shouldUpdate(
                    lastAircraftMarkerPoint, aircraftPoint,
                    lastAircraftMarkerHeading, aircraftMapHeading,
                    lastAircraftMarkerUpdateMs, SystemClock.elapsedRealtime())) {
                aircraftMarker.setPosition(position);
                aircraftMarker.setRotate(
                        FlightMapPresentation.mapMarkerRotationDegrees(aircraftMapHeading));
                lastAircraftMarkerPoint = aircraftPoint;
                lastAircraftMarkerHeading = aircraftMapHeading;
                lastAircraftMarkerUpdateMs = SystemClock.elapsedRealtime();
            }
            if (!aircraftMarkerVisible) {
                aircraftMarker.setVisible(true);
                aircraftMarkerVisible = true;
            }
        } else if (aircraftMarker != null) {
            if (aircraftMarkerVisible) aircraftMarker.setVisible(false);
            aircraftMarkerVisible = false;
        }
        updateRemoteControllerMapMarker(snapshot);
        updateHomeMapPresentation(snapshot);
        renderMapLocationStatus(snapshot);
        renderSurveyExecutionOverlay(false);
        if (surveyPlanningActive) return;

        long now = SystemClock.elapsedRealtime();
        String preferredSource = preferredMapFocusSource(snapshot);
        if (!preferredSource.equals(lastMapFocusSource)) {
            frameMapNearBestLiveLocation("source changed", mapFullscreen ? 17.5f : 16.8f,
                    true, true);
        } else if (!mapFullscreen && now - lastThumbnailMapFollowElapsedMs >= 2_000L) {
            lastThumbnailMapFollowElapsedMs = now;
            frameMapNearBestLiveLocation("thumbnail", 16.8f, false, false);
        } else if (!mapInitiallyFramed && !phoneMapLocationPending) {
            frameMapNearBestLiveLocation("initial", 17.0f, true, true);
        }
    }

    private void updateRemoteControllerMapMarker(Mini2AircraftBridge.Snapshot snapshot) {
        boolean rcLocationReady = hasRemoteControllerMapLocation(snapshot);
        boolean phoneLocationReady = isFreshPhoneMapLocation(lastPhoneMapLocation);
        GeoPoint rcLocation = rcLocationReady
                ? new GeoPoint(snapshot.getRcLatitude(), snapshot.getRcLongitude(), 0.0) : null;
        GeoPoint phoneLocation = phoneLocationReady
                ? new GeoPoint(lastPhoneMapLocation.getLatitude(),
                        lastPhoneMapLocation.getLongitude(), 0.0) : null;
        MapMarkerUpdatePolicy.DeviceLocation deviceLocation =
                MapMarkerUpdatePolicy.INSTANCE.preferredDeviceLocation(rcLocation, phoneLocation);
        if (deviceLocation == null) {
            if (remoteControllerMarker != null && remoteControllerMarkerVisible) {
                remoteControllerMarker.setVisible(false);
            }
            remoteControllerMarkerVisible = false;
            return;
        }
        GeoPoint rcPoint = deviceLocation.getPoint();
        String locationSource = deviceLocation.getSource().name();
        GeoPoint display = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(rcPoint);
        LatLng position = new LatLng(display.getLatitude(), display.getLongitude());
        PhoneHeadingSource.Heading deviceHeading = phoneHeading;
        boolean deviceHeadingReady = PhoneHeadingSource.isDisplayable(
                deviceHeading, SystemClock.elapsedRealtimeNanos());
        Double heading = rcLocationReady && snapshot.getRcCourseValid()
                ? Double.valueOf(snapshot.getRcCourseDegrees())
                : deviceHeadingReady ? Double.valueOf(deviceHeading.getDegrees()) : null;
        String headingSource = rcLocationReady && snapshot.getRcCourseValid() ? "RC_COURSE"
                : deviceHeadingReady ? "DEVICE" : "NONE";
        boolean courseModeChanged = remoteControllerMarker != null
                && !lastRemoteControllerHeadingSource.equals(headingSource);
        boolean locationModeChanged = remoteControllerMarker != null
                && !lastRemoteControllerLocationSource.equals(locationSource);
        boolean update = remoteControllerMarker == null || courseModeChanged || locationModeChanged
                || MapMarkerUpdatePolicy.INSTANCE.shouldUpdate(
                        lastRemoteControllerMarkerPoint, rcPoint,
                        lastRemoteControllerMarkerHeading, heading,
                        lastRemoteControllerMarkerUpdateMs, SystemClock.elapsedRealtime());
        if (remoteControllerMarker == null) {
            remoteControllerMarker = (Marker) amap.addOverlay(new MarkerOptions()
                    .position(position)
                    .title(getString(R.string.remote_controller))
                    .icon(remoteControllerArrowMapIcon())
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .zIndex(11));
        } else if (update) {
            remoteControllerMarker.setPosition(position);
        }
        if (update && heading != null) {
            remoteControllerMarker.setIcon(remoteControllerArrowMapIcon());
            remoteControllerMarker.setRotate(
                    FlightMapPresentation.mapMarkerRotationDegrees(heading));
            remoteControllerMarker.setTitle(getString(
                    "RC_COURSE".equals(headingSource)
                            ? R.string.rc_marker_moving : R.string.rc_marker_device_heading,
                    heading));
        } else if (update) {
            remoteControllerMarker.setIcon(remoteControllerStaticMapIcon());
            remoteControllerMarker.setRotate(0f);
            remoteControllerMarker.setTitle(getString(R.string.rc_marker_static));
        }
        if (update) {
            lastRemoteControllerMarkerPoint = rcPoint;
            lastRemoteControllerMarkerHeading = heading;
            lastRemoteControllerMarkerUpdateMs = SystemClock.elapsedRealtime();
            lastRemoteControllerHeadingSource = headingSource;
            lastRemoteControllerLocationSource = locationSource;
        }
        if (!remoteControllerMarkerVisible) {
            remoteControllerMarker.setVisible(true);
            remoteControllerMarkerVisible = true;
        }
    }

    private void updateHomeMapPresentation(Mini2AircraftBridge.Snapshot snapshot) {
        boolean homeValid = snapshot != null && snapshot.getConnected()
                && isUsableMapCoordinate(snapshot.getHomeLatitude(), snapshot.getHomeLongitude());
        if (!homeValid) {
            if (homeMarker != null) homeMarker.setVisible(false);
            if (homeDirectionLine != null) homeDirectionLine.setVisible(false);
            findViewById(R.id.home_direction_badge).setVisibility(View.GONE);
            return;
        }
        GeoPoint homePoint = new GeoPoint(
                snapshot.getHomeLatitude(), snapshot.getHomeLongitude(), 0.0);
        GeoPoint homeDisplay = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(homePoint);
        LatLng homePosition = new LatLng(homeDisplay.getLatitude(), homeDisplay.getLongitude());
        if (homeMarker == null) {
            homeMarker = (Marker) amap.addOverlay(new MarkerOptions()
                    .position(homePosition)
                    .title(getString(R.string.home_point_title))
                    .icon(homeMapIcon())
                    .anchor(0.5f, 0.5f)
                    .zIndex(10));
            lastHomeMarkerPoint = homePoint;
        } else {
            if (MapMarkerUpdatePolicy.INSTANCE.shouldMove(lastHomeMarkerPoint, homePoint)) {
                homeMarker.setPosition(homePosition);
                lastHomeMarkerPoint = homePoint;
            }
            homeMarker.setVisible(true);
        }
        GeoPoint aircraftPoint = effectiveAircraftGeoPoint(snapshot);
        boolean showDirection = snapshot.getFlying() && aircraftPoint != null;
        TextView badge = findViewById(R.id.home_direction_badge);
        if (!showDirection) {
            if (homeDirectionLine != null) homeDirectionLine.setVisible(false);
            badge.setVisibility(View.GONE);
            return;
        }
        GeoPoint aircraftDisplay = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(
                aircraftPoint);
        LatLng aircraftPosition = new LatLng(
                aircraftDisplay.getLatitude(), aircraftDisplay.getLongitude());
        List<LatLng> homePath = java.util.Arrays.asList(aircraftPosition, homePosition);
        if (homeDirectionLine == null) {
            homeDirectionLine = (Polyline) amap.addOverlay(new PolylineOptions()
                    .points(homePath)
                    .width(dp(2))
                    .color(0xFFF29A2E)
                    .dottedLine(true)
                    .isGeodesic(true)
                    .zIndex(8));
            lastHomeLineAircraftPoint = aircraftPoint;
            lastHomeLinePoint = homePoint;
        } else {
            boolean aircraftMoved = MapMarkerUpdatePolicy.INSTANCE.shouldMove(
                    lastHomeLineAircraftPoint, aircraftPoint);
            boolean homeMoved = MapMarkerUpdatePolicy.INSTANCE.shouldMove(
                    lastHomeLinePoint, homePoint);
            if (aircraftMoved || homeMoved) {
                homeDirectionLine.setPoints(homePath);
                lastHomeLineAircraftPoint = aircraftPoint;
                lastHomeLinePoint = homePoint;
            }
            homeDirectionLine.setVisible(true);
        }
        double bearing = FlightMapPresentation.bearingDegrees(
                aircraftPoint.getLatitude(), aircraftPoint.getLongitude(),
                snapshot.getHomeLatitude(), snapshot.getHomeLongitude());
        double distance = FlightMapPresentation.distanceMeters(
                aircraftPoint.getLatitude(), aircraftPoint.getLongitude(),
                snapshot.getHomeLatitude(), snapshot.getHomeLongitude());
        badge.setText(String.format(Locale.US, "H %s %.0f m",
                FlightMapPresentation.directionArrow(bearing), distance));
        badge.setVisibility(View.VISIBLE);
    }

    private void renderMapLocationStatus(Mini2AircraftBridge.Snapshot snapshot) {
        TextView status = findViewById(R.id.map_location_status);
        if (status == null) return;
        if (hasAircraftMapLocation(snapshot) || hasRemoteControllerMapLocation(snapshot)) {
            status.setVisibility(View.GONE);
        } else if (isUsablePhoneLocation(lastPhoneMapLocation)) {
            status.setText(mapBaseLoaded
                    ? R.string.map_phone_waiting_gps : R.string.map_loading);
            status.setVisibility(View.VISIBLE);
        } else {
            status.setText(getString(R.string.map_waiting_for_location));
            status.setVisibility(View.VISIBLE);
        }
    }

    private String preferredMapFocusSource(Mini2AircraftBridge.Snapshot snapshot) {
        if (hasAircraftMapLocation(snapshot)) return "AIRCRAFT";
        if (hasRemoteControllerMapLocation(snapshot)) return "REMOTE_CONTROLLER";
        if (isUsablePhoneLocation(lastPhoneMapLocation)) return "PHONE";
        return "NONE";
    }

    private BitmapDescriptor navigationArrowIcon(int fillColor) {
        int size = dp(30);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(fillColor);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2f,
                1.5f * getResources().getDisplayMetrics().density));
        stroke.setStrokeJoin(Paint.Join.ROUND);
        stroke.setColor(Color.WHITE);
        Path arrow = new Path();
        arrow.moveTo(size * 0.50f, size * 0.08f);
        arrow.lineTo(size * 0.82f, size * 0.82f);
        arrow.lineTo(size * 0.50f, size * 0.68f);
        arrow.lineTo(size * 0.18f, size * 0.82f);
        arrow.close();
        canvas.drawPath(arrow, fill);
        canvas.drawPath(arrow, stroke);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor aircraftMapIcon() {
        if (aircraftMapIcon == null) aircraftMapIcon = navigationArrowIcon(0xFF2479D0);
        return aircraftMapIcon;
    }

    private BitmapDescriptor remoteControllerArrowMapIcon() {
        if (remoteControllerArrowMapIcon == null) {
            remoteControllerArrowMapIcon = navigationArrowIcon(0xFF28A66A);
        }
        return remoteControllerArrowMapIcon;
    }

    private BitmapDescriptor remoteControllerStaticMapIcon() {
        if (remoteControllerStaticMapIcon == null) {
            remoteControllerStaticMapIcon = surveyMarkerIcon("RC", 0xFF28A66A);
        }
        return remoteControllerStaticMapIcon;
    }

    private BitmapDescriptor homeMapIcon() {
        if (homeMapIcon == null) homeMapIcon = surveyMarkerIcon("H", 0xFFF29A2E);
        return homeMapIcon;
    }

    private void frameMapAtAircraft() {
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        GeoPoint aircraftPoint = effectiveAircraftGeoPoint(snapshot);
        if (amap == null || aircraftPoint == null) {
            renderSurveyStatus(getString(R.string.survey_aircraft_location_not_ready));
            return;
        }
        GeoPoint displayPoint = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(
                aircraftPoint);
        mapInitiallyFramed = true;
        amap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
                new MapStatus.Builder()
                        .target(new LatLng(displayPoint.getLatitude(), displayPoint.getLongitude()))
                        .zoom(18.0f)
                        .build()), 500);
        renderSurveyStatus(getString(R.string.survey_aircraft_located_add_boundary));
        appendLog("MAP framed at aircraft for survey planning");
    }

    /** Frames the initial map near a fresh Android phone fix before falling back to aircraft telemetry. */
    private void frameMapNearPhone() {
        if (amap == null || phoneMapLocationResolved || phoneMapLocationPending) return;
        if (!hasPhoneLocationPermission()) {
            beginPhoneMapLocationWait();
            return;
        }
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (manager == null) {
            resolvePhoneMapLocationWithoutFix("location service unavailable");
            return;
        }

        Location best = null;
        for (String provider : manager.getProviders(true)) {
            try {
                Location candidate = manager.getLastKnownLocation(provider);
                if (isFreshPhoneMapLocation(candidate)
                        && (best == null || candidate.getTime() > best.getTime())) {
                    best = candidate;
                }
            } catch (SecurityException ignored) {
                resolvePhoneMapLocationWithoutFix("location permission rejected by provider");
                return;
            }
        }
        if (best != null) {
            frameMapAtPhoneLocation(best);
            return;
        }

        beginPhoneMapLocationWait();
        boolean networkRequested = requestSinglePhoneLocation(manager, LocationManager.NETWORK_PROVIDER);
        boolean gpsRequested = requestSinglePhoneLocation(manager, LocationManager.GPS_PROVIDER);
        phoneMapLocationRequested = networkRequested || gpsRequested;
        if (!phoneMapLocationRequested) {
            resolvePhoneMapLocationWithoutFix("no enabled location provider");
        }
    }

    private boolean hasPhoneLocationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void beginPhoneMapLocationWait() {
        phoneMapLocationPending = true;
        mainHandler.removeCallbacks(phoneMapLocationTimeoutRunnable);
        mainHandler.postDelayed(phoneMapLocationTimeoutRunnable, PHONE_MAP_LOCATION_TIMEOUT_MS);
    }

    private void resolvePhoneMapLocationWithoutFix(String reason) {
        mainHandler.removeCallbacks(phoneMapLocationTimeoutRunnable);
        phoneMapLocationPending = false;
        phoneMapLocationResolved = true;
        appendLog("MAP phone GPS unavailable (" + reason + "); using aircraft when available");
        updateMap(aircraftSnapshot);
    }

    private boolean requestSinglePhoneLocation(LocationManager manager, String provider) {
        try {
            if (!manager.isProviderEnabled(provider)) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(provider, null, getMainExecutor(), location -> {
                    if (isUsablePhoneLocation(location)) frameMapAtPhoneLocation(location);
                });
                return true;
            }
            manager.requestSingleUpdate(provider, new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    if (isUsablePhoneLocation(location)) frameMapAtPhoneLocation(location);
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
            }, Looper.getMainLooper());
            return true;
        } catch (SecurityException | IllegalArgumentException ignored) {
            Log.w(TAG, "phone map location unavailable from " + provider);
            return false;
        }
    }

    private boolean isUsablePhoneLocation(Location location) {
        return location != null
                && Double.isFinite(location.getLatitude())
                && Double.isFinite(location.getLongitude())
                && location.getLatitude() >= -90.0 && location.getLatitude() <= 90.0
                && location.getLongitude() >= -180.0 && location.getLongitude() <= 180.0
                && !(Math.abs(location.getLatitude()) < 1.0e-9
                && Math.abs(location.getLongitude()) < 1.0e-9);
    }

    private boolean isFreshPhoneMapLocation(Location location) {
        if (!isUsablePhoneLocation(location)) return false;
        long ageMillis = Math.max(0L, System.currentTimeMillis() - location.getTime());
        return ageMillis <= PHONE_MAP_LAST_KNOWN_MAX_AGE_MS
                && (!location.hasAccuracy()
                || location.getAccuracy() <= PHONE_MAP_LAST_KNOWN_MAX_ACCURACY_METERS);
    }

    private void frameMapAtPhoneLocation(Location location) {
        if (amap == null || !isUsablePhoneLocation(location)) return;
        mainHandler.removeCallbacks(phoneMapLocationTimeoutRunnable);
        phoneMapLocationPending = false;
        phoneMapLocationResolved = true;
        lastPhoneMapLocation = new Location(location);
        if (hasAircraftMapLocation(aircraftSnapshot)
                || hasRemoteControllerMapLocation(aircraftSnapshot)) {
            appendLog("MAP phone GPS cached; live DJI position retained");
            renderMapLocationStatus(aircraftSnapshot);
            return;
        }
        frameMapAtWgs84(location.getLatitude(), location.getLongitude(), 17.0f, true);
        lastMapFocusSource = "PHONE";
        renderMapLocationStatus(aircraftSnapshot);
        appendLog("MAP focus=PHONE zoom=17.0");
    }

    private boolean hasAircraftMapLocation(Mini2AircraftBridge.Snapshot snapshot) {
        return effectiveAircraftGeoPoint(snapshot) != null;
    }

    private GeoPoint effectiveAircraftGeoPoint(Mini2AircraftBridge.Snapshot snapshot) {
        SurveySimulatorMapPose pose = effectiveAircraftMapPose(snapshot);
        return pose == null ? null : pose.getPoint();
    }

    private SurveySimulatorMapPose effectiveAircraftMapPose(Mini2AircraftBridge.Snapshot snapshot) {
        if (snapshot == null) return null;
        if (snapshot.getConnected()
                && isUsableMapCoordinate(snapshot.getLatitude(), snapshot.getLongitude())) {
            return new SurveySimulatorMapPose(
                    new GeoPoint(snapshot.getLatitude(), snapshot.getLongitude(), snapshot.getAltitude()),
                    snapshot.getHeading());
        }
        return freshSimulatorMapPose(snapshot);
    }

    private SurveySimulatorMapPose freshSimulatorMapPose(Mini2AircraftBridge.Snapshot snapshot) {
        if (snapshot == null || aircraftBridge == null) return null;
        Mini2AircraftBridge.SimulatorSample sample = aircraftBridge.currentSimulatorSample();
        if (sample == null) return null;
        return SurveySimulatorMapProjection.INSTANCE.project(
                snapshot.getSimulatorActive(), sample.getElapsedRealtimeNanos(),
                SystemClock.elapsedRealtimeNanos(), HIL_ASYNC_FRAME_FRESH_MILLIS * 1_000_000L,
                sample.getOriginLatitude(), sample.getOriginLongitude(),
                sample.getEastMeters(), sample.getNorthMeters(), sample.getDownMeters(),
                sample.getYawDegrees());
    }

    private boolean hasRemoteControllerMapLocation(Mini2AircraftBridge.Snapshot snapshot) {
        return snapshot != null && snapshot.getRcLocationValid()
                && isUsableMapCoordinate(snapshot.getRcLatitude(), snapshot.getRcLongitude());
    }

    private boolean isUsableMapCoordinate(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0
                && !(Math.abs(latitude) < 1.0e-9 && Math.abs(longitude) < 1.0e-9);
    }

    /** Aircraft first, then controller GPS, then the Android device attached to the controller. */
    private boolean frameMapNearBestLiveLocation(
            String reason, float zoom, boolean animate, boolean writeLog) {
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        String source;
        double latitude;
        double longitude;
        GeoPoint effectiveAircraft = effectiveAircraftGeoPoint(snapshot);
        if (effectiveAircraft != null) {
            source = "AIRCRAFT";
            latitude = effectiveAircraft.getLatitude();
            longitude = effectiveAircraft.getLongitude();
        } else if (hasRemoteControllerMapLocation(snapshot)) {
            source = "REMOTE_CONTROLLER";
            latitude = snapshot.getRcLatitude();
            longitude = snapshot.getRcLongitude();
        } else if (isUsablePhoneLocation(lastPhoneMapLocation)) {
            source = "PHONE";
            latitude = lastPhoneMapLocation.getLatitude();
            longitude = lastPhoneMapLocation.getLongitude();
        } else {
            return false;
        }
        frameMapAtWgs84(latitude, longitude, zoom, animate);
        lastMapFocusSource = source;
        if (writeLog) appendLog("MAP " + reason + " focus=" + source + " zoom=" + zoom);
        return true;
    }

    private void frameMapAtWgs84(double latitude, double longitude, float zoom, boolean animate) {
        if (amap == null || !isUsableMapCoordinate(latitude, longitude)) return;
        GeoPoint displayPoint = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(
                new GeoPoint(latitude, longitude, 0.0));
        MapStatus camera = new MapStatus.Builder()
                .target(new LatLng(displayPoint.getLatitude(), displayPoint.getLongitude()))
                .zoom(zoom)
                .build();
        mapInitiallyFramed = true;
        if (animate) {
            amap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(camera), 500);
        } else {
            amap.setMapStatus(MapStatusUpdateFactory.newMapStatus(camera));
        }
    }

    private void showSurveyPlannerTab(int tab) {
        surveyPlannerTab = Math.max(SURVEY_TAB_AREA, Math.min(SURVEY_TAB_TERRAIN, tab));
        View area = findViewById(R.id.survey_area_section);
        View route = findViewById(R.id.survey_route_section);
        View capture = findViewById(R.id.survey_capture_section);
        View terrain = findViewById(R.id.survey_terrain_section);
        if (area == null || route == null || capture == null || terrain == null) return;
        area.setVisibility(surveyPlannerTab == SURVEY_TAB_AREA ? View.VISIBLE : View.GONE);
        route.setVisibility(surveyPlannerTab == SURVEY_TAB_ROUTE ? View.VISIBLE : View.GONE);
        capture.setVisibility(surveyPlannerTab == SURVEY_TAB_CAPTURE ? View.VISIBLE : View.GONE);
        terrain.setVisibility(surveyPlannerTab == SURVEY_TAB_TERRAIN ? View.VISIBLE : View.GONE);

        if (surveyPlannerTab == SURVEY_TAB_TERRAIN && surveyTerrain == null) {
            renderSurveyStatus(getString(surveyRoi.size() < 3
                    ? R.string.terrain_step_draw_boundary
                    : R.string.terrain_step_load_elevation));
        }

        View editActions = findViewById(R.id.survey_edit_actions_row);
        View generateActions = findViewById(R.id.survey_generate_actions_row);
        View captureSelector = findViewById(R.id.survey_capture_view_selector);
        editActions.setVisibility(surveyPlannerTab == SURVEY_TAB_AREA ? View.VISIBLE : View.GONE);
        generateActions.setVisibility(surveyPlannerTab == SURVEY_TAB_AREA ? View.GONE : View.VISIBLE);
        captureSelector.setVisibility(surveyPlannerTab == SURVEY_TAB_CAPTURE ? View.VISIBLE : View.GONE);
        renderSurveyPlannerTabs();

        android.widget.ScrollView scroll = findViewById(R.id.survey_planner_scroll);
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    private void renderSurveyPlannerTabs() {
        CheckBox terrainMode = findViewById(R.id.survey_terrain_enabled_checkbox);
        if (terrainMode != null) {
            terrainMode.setTextColor(ColorStateList.valueOf(0xFF244A68));
        }
        CheckBox buildingConfirm = findViewById(R.id.survey_dsm_building_confirm);
        if (buildingConfirm != null) {
            buildingConfirm.setTextColor(ColorStateList.valueOf(0xFF505862));
        }
        Button terrainTab = findViewById(R.id.survey_tab_terrain_button);
        if (terrainTab != null) {
            terrainTab.setText(surveyTerrainFollowingEnabled
                    ? getString(R.string.survey_terrain_on) : getString(R.string.terrain));
        }
        int[] ids = new int[] {
                R.id.survey_tab_area_button,
                R.id.survey_tab_route_button,
                R.id.survey_tab_capture_button,
                R.id.survey_tab_terrain_button
        };
        for (int index = 0; index < ids.length; index++) {
            Button button = findViewById(ids[index]);
            if (button == null) continue;
            boolean selected = index == surveyPlannerTab;
            styleButton(button,
                    selected ? 0xFF1F6FB2 : 0xFFF3F5F7,
                    selected ? 0xFF155B96 : 0xFFD8DDE3,
                    5);
            button.setTextColor(selected ? 0xFFFFFFFF : 0xFF56606B);
        }
    }

    private void showSurveyObliqueAngleDialog() {
        final String[] labels = new String[] {
                "-30°", getString(R.string.survey_pitch_recommended_value), "-60°", "-75°"
        };
        final String[] values = new String[] {"-30", "-45", "-60", "-75"};
        double current = -45.0;
        try {
            current = parseNumber((EditText) findViewById(R.id.survey_gimbal_input));
        } catch (IllegalArgumentException ignored) {
            // Keep the recommended selection when the manually entered value is incomplete.
        }
        int selected = 0;
        double smallestDifference = Double.MAX_VALUE;
        for (int index = 0; index < values.length; index++) {
            double difference = Math.abs(current - Double.parseDouble(values[index]));
            if (difference < smallestDifference) {
                smallestDifference = difference;
                selected = index;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_oblique_pitch_title)
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    ((EditText) findViewById(R.id.survey_gimbal_input)).setText(values[which]);
                    renderSurveyObliqueAngleButton();
                    schedulePersistSurveyPlannerSettings();
                    renderSurveyStatus(getString(R.string.survey_oblique_pitch_set, labels[which]));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void renderSurveyObliqueAngleButton() {
        Button button = findViewById(R.id.survey_oblique_angle_button);
        EditText input = findViewById(R.id.survey_gimbal_input);
        if (button == null || input == null) return;
        String value = input.getText().toString().trim();
        button.setText(value.isEmpty()
                ? getString(R.string.survey_set_pitch)
                : getString(R.string.survey_pitch_value, value));
    }

    private void renderSurveyTerrainModeControl() {
        CheckBox checkbox = findViewById(R.id.survey_terrain_enabled_checkbox);
        if (checkbox != null && checkbox.isChecked() != surveyTerrainFollowingEnabled) {
            checkbox.setChecked(surveyTerrainFollowingEnabled);
        }
        renderSurveyPlannerTabs();
    }

    private void setSurveyTerrainFollowingEnabled(boolean enabled, boolean userInitiated) {
        boolean changed = surveyTerrainFollowingEnabled != enabled;
        surveyTerrainFollowingEnabled = enabled;
        renderSurveyTerrainModeControl();
        if (!changed) return;
        schedulePersistSurveyPlannerSettings();
        if (userInitiated) {
            abortSurveySimulatorExecution(getString(R.string.reason_switch_altitude_mode), true);
            stopSurveyReplay(true);
            surveyMission = null;
            clearPersistedSurveySession();
            renderSurveyOverlay();
            if (enabled) {
                renderSurveyStatus(getString(surveyTerrain == null
                        ? R.string.survey_terrain_selected_needs_data
                        : R.string.survey_terrain_enabled_regenerate));
            } else {
                renderSurveyStatus(getString(R.string.survey_fixed_altitude_restored));
            }
            appendLog("SURVEY terrain-follow mode=" + (enabled ? "ENABLED" : "FIXED_AGL"));
        }
    }

    private void showSurveyPlannerMoreMenu() {
        String[] actions = new String[] {
                getString(R.string.survey_map_view_action,
                        getString(mapThreeDimensional
                                ? R.string.survey_map_switch_2d : R.string.survey_map_switch_3d)),
                getString(R.string.survey_real_readiness_audit),
                getString(R.string.survey_gimbal_nadir_diagnostic),
                getString(R.string.survey_gimbal_oblique_diagnostic),
                getString(R.string.survey_save_version), getString(R.string.survey_mission_library),
                getString(R.string.survey_stop_preview), getString(R.string.survey_import_mission),
                getString(R.string.survey_export_mission)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_more_title)
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            setSurveyMapThreeDimensional(!mapThreeDimensional);
                            break;
                        case 1:
                            auditSurveyRealFlightReadiness();
                            break;
                        case 2:
                            runSurveyGimbalDiagnostic(-90.0);
                            break;
                        case 3:
                            runSurveyGimbalDiagnostic(-45.0);
                            break;
                        case 4:
                            saveSurveyMissionVersion(true);
                            break;
                        case 5:
                            showSurveyMissionLibrary();
                            break;
                        case 6:
                            stopSurveyReplay(true);
                            break;
                        case 7:
                            chooseSurveyMission();
                            break;
                        case 8:
                            exportSurveyMission();
                            break;
                        default:
                            break;
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void runSurveyGimbalDiagnostic(double targetPitchDegrees) {
        if (surveySimulatorExecution != null
                && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.RUNNING) {
            renderSurveyStatus(getString(R.string.survey_pause_before_gimbal_diagnostic));
            return;
        }
        if (!aircraftSnapshot.getConnected()) {
            renderSurveyStatus(getString(R.string.survey_gimbal_diagnostic_disconnected));
            return;
        }
        aircraftBridge.setSurveyGimbalPitch(targetPitchDegrees);
        appendLog(String.format(Locale.US,
                "SURVEY gimbal diagnostic command target=%.0f° ignore=true", targetPitchDegrees));
        renderSurveyStatus(getString(R.string.survey_gimbal_diagnostic_sent, targetPitchDegrees));
        mainHandler.postDelayed(() -> appendSurveyGimbalDiagnostic(targetPitchDegrees, 2), 2_000L);
        mainHandler.postDelayed(() -> appendSurveyGimbalDiagnostic(targetPitchDegrees, 5), 5_000L);
    }

    private void appendSurveyGimbalDiagnostic(double targetPitchDegrees, int elapsedSeconds) {
        appendLog(String.format(Locale.US,
                "SURVEY gimbal diagnostic t=%ds target=%.0f° actual=%.1f° aircraftPitch=%+.1f° "
                        + "pitchAtStop=%s motorOverloaded=%s mode=%s",
                elapsedSeconds, targetPitchDegrees, aircraftSnapshot.getGimbalPitch(),
                aircraftSnapshot.getAircraftPitch(), aircraftSnapshot.getGimbalPitchAtStop(),
                aircraftSnapshot.getGimbalMotorOverloaded(), aircraftSnapshot.getGimbalMode()));
    }

    private void setSurveyMapThreeDimensional(boolean enabled) {
        if (amap == null) return;
        mapThreeDimensional = enabled;
        Button button = findViewById(R.id.survey_map_3d_button);
        if (button != null) {
            button.setText(enabled ? "2D" : "3D");
            button.setContentDescription(getString(enabled
                    ? R.string.map_switch_2d_description : R.string.map_toggle_3d_description));
        }
        MapStatus current = amap.getMapStatus();
        MapStatus next = new MapStatus.Builder(current)
                .zoom(enabled ? Math.max(17.3f, current.zoom) : current.zoom)
                .overlook(enabled ? -55.0f : 0.0f)
                .build();
        amap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(next), 500);
        renderSurveyStatus(getString(enabled
                ? R.string.map_3d_visual_only_status : R.string.map_2d_restored_status));
        appendLog("SURVEY map view=" + (enabled ? "3D_VISUAL_ONLY" : "2D"));
    }

    private void showSurveyExecutionStatusDetails() {
        TextView status = findViewById(R.id.survey_sim_gate_status);
        CharSequence detail = status == null ? getString(R.string.survey_preflight_not_run) : status.getText();
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_preflight_execution_title)
                .setMessage(detail)
                .setPositiveButton(R.string.action_got_it, null)
                .show();
    }

    private void renderV86Status(V86StreamingController.Snapshot snapshot) {
        TextView status = findViewById(R.id.v86_status_text);
        if (status == null) return;
        if (snapshot.getSessionId() == null) {
            boolean configured = v86Controller != null && !v86Controller.accessCode().isEmpty();
            if (snapshot.getError() != null) {
                status.setText(getString(R.string.v86_action_failed, snapshot.getError()));
                status.setTextColor(0xFFC62828);
            } else {
                status.setText(configured ? R.string.v86_ready_status : R.string.v86_not_configured);
                status.setTextColor(0xFF66717D);
            }
            return;
        }
        status.setText(getString(R.string.v86_streaming_status,
                snapshot.getCapturedCount(), snapshot.getUploadedCount(),
                snapshot.getPendingCount(), snapshot.getRejectedCount())
                + "\n" + snapshot.getMessage());
        status.setTextColor(snapshot.getError() == null ? 0xFF66717D : 0xFFC62828);
    }

    private void showV86Dialog() {
        if (v86Controller == null) return;
        if (v86Controller.current().getSessionId() == null) showV86CreateDialog();
        else showV86ActionsDialog();
    }

    private void showV86CreateDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(8), dp(18), 0);
        EditText endpoint = new EditText(this);
        endpoint.setHint(R.string.v86_service_address);
        endpoint.setSingleLine(true);
        endpoint.setText(v86Controller.current().getEndpoint());
        EditText token = new EditText(this);
        token.setHint(R.string.v86_access_code);
        token.setSingleLine(true);
        token.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText name = new EditText(this);
        name.setHint(R.string.v86_session_name);
        name.setSingleLine(true);
        name.setText(surveyMission == null
                ? getString(R.string.survey_default_mission_name) : surveyMission.getName());
        EditText asl = new EditText(this);
        asl.setHint(R.string.v86_takeoff_asl);
        asl.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        if (Double.isFinite(aircraftSnapshot.getAsl())) {
            asl.setText(String.format(Locale.US, "%.2f", aircraftSnapshot.getAsl()));
        }
        TextView warning = new TextView(this);
        warning.setText(R.string.v86_insecure_http_warning);
        warning.setTextColor(0xFFC62828);
        content.addView(endpoint); content.addView(token); content.addView(name); content.addView(asl);
        content.addView(warning);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.v86_cloud_reconstruction)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setNeutralButton(R.string.v86_save_connection, null)
                .setPositiveButton(R.string.v86_create_session, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                String error = v86Controller.saveConnectionError(
                        endpoint.getText().toString(), token.getText().toString());
                if (error == null) {
                    showBanner(getString(R.string.v86_connection_saved));
                    dialog.dismiss();
                } else showBanner(getString(R.string.v86_action_failed, error));
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {
                    String service = endpoint.getText().toString();
                    String accessCode = token.getText().toString();
                    double takeoffAsl = Double.parseDouble(asl.getText().toString().trim());
                    String saveError = v86Controller.saveConnectionError(service, accessCode);
                    if (saveError != null) throw new IllegalArgumentException(saveError);
                    CameraProfile camera = currentSurveyCameraProfile();
                    V86SessionConfig config = new V86SessionConfig(
                            name.getText().toString().trim(),
                            camera.getHorizontalFieldOfViewDegrees(), takeoffAsl,
                            aircraftBridge == null ? "DJI" : aircraftBridge.currentSurveyCameraProfileLabel(),
                            true, true, 10);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    v86Controller.createSessionJava(config, (session, createError) -> {
                        if (createError == null && session != null) {
                            dialog.dismiss();
                            showBanner(getString(R.string.v86_session_created, session.getId()));
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            showBanner(getString(R.string.v86_action_failed,
                                    createError == null ? getString(R.string.state_unrecognized) : createError));
                        }
                    });
                } catch (Throwable error) {
                    showBanner(getString(R.string.v86_action_failed,
                            error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()));
                }
            });
        });
        dialog.show();
    }

    private void showV86ActionsDialog() {
        V86WorkflowState workflow = V86WorkflowPolicy.INSTANCE.from(v86Controller.current());
        String[] actions = new String[] {
                getString(R.string.v86_edit_connection), getString(R.string.v86_refresh),
                getString(R.string.v86_retry_uploads),
                getString(R.string.v86_finalize), getString(R.string.v86_retry_processing),
                getString(R.string.v86_fetch_result), getString(R.string.v86_import_mission),
                getString(R.string.v86_open_point_cloud), getString(R.string.v86_offline_replay),
                getString(R.string.v86_terminate_session), getString(R.string.v86_discard_empty_session),
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.v86_session_actions)
                .setMessage(workflow.getTitle() + "\n" + workflow.getDetail())
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: showV86ConnectionDialog(); break;
                        case 1: v86Controller.refresh(); break;
                        case 2: v86Controller.retryUploads(); break;
                        case 3:
                            if (workflow.getCanFinalize()) v86Controller.finalizeSession();
                            else showBanner(getString(R.string.v86_finalize_not_ready));
                            break;
                        case 4: v86Controller.retryProcessing(); break;
                        case 5: v86Controller.fetchResult(); break;
                        case 6: importV86Mission(); break;
                        case 7: openV86PointCloud(); break;
                        case 8: chooseV86OfflineFolder(); break;
                        case 9: confirmTerminateV86(false); break;
                        case 10:
                            if (workflow.getCanDiscardEmpty()) confirmTerminateV86(true);
                            else showBanner(getString(R.string.v86_discard_not_empty));
                            break;
                        default: break;
                    }
                })
                .setNegativeButton(R.string.action_close, null)
                .show();
    }

    private void showV86ConnectionDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(8), dp(18), 0);
        EditText endpoint = new EditText(this);
        endpoint.setHint(R.string.v86_service_address);
        endpoint.setSingleLine(true);
        endpoint.setText(v86Controller.current().getEndpoint());
        EditText token = new EditText(this);
        token.setHint(R.string.v86_access_code);
        token.setSingleLine(true);
        token.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        content.addView(endpoint);
        content.addView(token);
        new AlertDialog.Builder(this)
                .setTitle(R.string.v86_edit_connection)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.v86_save_connection, (dialog, which) -> {
                    String error = v86Controller.saveConnectionError(
                            endpoint.getText().toString(), token.getText().toString());
                    if (error == null) {
                        showBanner(getString(R.string.v86_connection_saved));
                        v86Controller.refresh();
                        v86Controller.retryUploads();
                    } else showBanner(getString(R.string.v86_action_failed, error));
                })
                .show();
    }

    private void confirmTerminateV86(boolean emptyOnly) {
        new AlertDialog.Builder(this)
                .setTitle(emptyOnly ? R.string.v86_discard_empty_session
                        : R.string.v86_terminate_session)
                .setMessage(emptyOnly ? R.string.v86_discard_empty_message
                        : R.string.v86_terminate_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    if (emptyOnly) {
                        String error = v86Controller.discardEmptySessionError();
                        if (error != null) showBanner(getString(R.string.v86_action_failed, error));
                    } else v86Controller.terminateCurrentSession();
                }).show();
    }

    private void importV86Mission() {
        V86Result cloudResult = v86Controller.current().getResult();
        if (cloudResult != null && !cloudResult.getSafeToExecute()) {
            showBanner(getString(R.string.v86_imported_mission_safe_false));
            return;
        }
        v86Controller.downloadMissionJava((value, downloadError) -> {
            if (downloadError != null || value == null) {
                showBanner(getString(R.string.v86_action_failed, downloadError));
                return;
            }
            try {
                SurveyMission imported = SurveyMissionJson.INSTANCE.decode(value);
                if (imported.getActiveMapping() != null) {
                    edu.playground.djivln.survey.ActiveRecaptureMissionValidator.validate(imported);
                }
                activateSurveyMission(imported, getString(R.string.imported));
                saveSurveyMissionVersion(false);
            } catch (Throwable error) {
                showBanner(getString(R.string.v86_action_failed, error.getMessage()));
            }
        });
    }

    private void openV86PointCloud() {
        V86Result result = v86Controller.current().getResult();
        if (result == null || result.getPointCloudUrl() == null) {
            v86Controller.fetchResult();
            showBanner(getString(R.string.v86_point_cloud_not_ready));
            return;
        }
        File root = new File(getFilesDir(), "v86-point-cloud");
        root.mkdirs();
        String cacheStem = V86PointCloudCachePolicy.INSTANCE.fileStem(v86Controller.current());
        V86PointCloudCachePolicy.INSTANCE.prune(root, cacheStem, 3,
                768L * 1024L * 1024L);
        File ply = new File(root, cacheStem + ".ply");
        File candidates = new File(root, cacheStem + "-viewer.json");
        v86Controller.downloadViewerDataJava((value, error) -> {
            if (value == null) {
                downloadAndOpenV86PointCloud(ply, null);
                return;
            }
            storageExecutor.execute(() -> {
                try {
                    try (OutputStream output = new FileOutputStream(candidates)) {
                        output.write(value.getBytes(StandardCharsets.UTF_8));
                    }
                    runOnUiThread(() -> downloadAndOpenV86PointCloud(ply, candidates));
                } catch (Throwable ignored) {
                    runOnUiThread(() -> downloadAndOpenV86PointCloud(ply, null));
                }
            });
        });
    }

    private void downloadAndOpenV86PointCloud(File target, File candidates) {
        if (V86PointCloudCachePolicy.INSTANCE.isUsablePly(target)) {
            target.setLastModified(System.currentTimeMillis());
            openV86PointCloudFile(target, candidates);
            return;
        }
        lastV86DownloadPercent = -1;
        v86Controller.downloadPointCloudToJava(target, (downloaded, total) -> {
            if (total == null || total <= 0L) return;
            int percent = (int) Math.min(100L, downloaded * 100L / total);
            if (percent / 10 == lastV86DownloadPercent / 10) return;
            lastV86DownloadPercent = percent;
            runOnUiThread(() -> {
                TextView status = findViewById(R.id.v86_status_text);
                if (status != null) status.setText(getString(
                        R.string.v86_point_cloud_download_progress, percent));
            });
        }, (file, error) -> {
            if (file == null) {
                showBanner(getString(R.string.v86_action_failed, error));
                return;
            }
            openV86PointCloudFile(file, candidates);
        });
    }

    private void openV86PointCloudFile(File file, File candidates) {
        Intent intent = new Intent(this, V86PointCloudActivity.class)
                .putExtra(V86PointCloudActivity.EXTRA_PLY_PATH, file.getAbsolutePath());
        if (candidates != null && candidates.isFile()) {
            intent.putExtra(V86PointCloudActivity.EXTRA_CANDIDATES_PATH,
                    candidates.getAbsolutePath());
        }
        startActivity(intent);
    }

    private void chooseV86OfflineFolder() {
        if (v86Controller == null || v86Controller.current().getSessionId() == null) {
            showBanner(getString(R.string.v86_offline_requires_session));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, V86_OFFLINE_FOLDER_REQUEST);
    }

    private void replayV86OfflineFolder(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Throwable ignored) { }
        DocumentFile directory = DocumentFile.fromTreeUri(this, uri);
        if (directory == null || !directory.isDirectory()) {
            showBanner(getString(R.string.v86_offline_folder_invalid));
            return;
        }
        DocumentFile[] files = directory.listFiles();
        java.util.Arrays.sort(files, (first, second) ->
                String.valueOf(first.getName()).compareToIgnoreCase(String.valueOf(second.getName())));
        List<DocumentFile> images = new ArrayList<>();
        for (DocumentFile file : files) {
            String name = file.getName();
            if (file.isFile() && name != null &&
                    (name.toLowerCase(Locale.US).endsWith(".jpg")
                            || name.toLowerCase(Locale.US).endsWith(".jpeg"))) images.add(file);
        }
        if (images.isEmpty()) {
            showBanner(getString(R.string.v86_offline_no_jpeg));
            return;
        }
        TextView status = findViewById(R.id.v86_status_text);
        status.setText(getString(R.string.v86_offline_preflight, images.size()));
        storageExecutor.execute(() -> {
            V86OfflineImageCompressor compressor = new V86OfflineImageCompressor(this);
            String expectedSessionId = v86Controller.current().getSessionId();
            File stagingDirectory = new File(getCacheDir(),
                    "v86-offline-stage-" + System.nanoTime());
            List<File> stagedFiles = new ArrayList<>();
            List<V86OfflineImageCompressor.PreparedImage> stagedMetadata = new ArrayList<>();
            int queued = 0;
            try {
                if (!stagingDirectory.mkdirs() && !stagingDirectory.isDirectory()) {
                    throw new IllegalStateException(getString(R.string.cannot_create_directory));
                }
                // Prepare the complete batch on private disk before the first queue/upload mutation.
                for (DocumentFile image : images) {
                    V86OfflineImageCompressor.PreparedImage prepared;
                    try (InputStream input = getContentResolver().openInputStream(image.getUri())) {
                        if (input == null) throw new IllegalArgumentException(
                                getString(R.string.cannot_read_file));
                        prepared = compressor.prepare(String.valueOf(image.getName()), input);
                    }
                    File staged = new File(stagingDirectory,
                            String.format(Locale.US, "%08d.jpg", stagedFiles.size()));
                    try (java.io.FileOutputStream output = new java.io.FileOutputStream(staged)) {
                        output.write(prepared.getBytes());
                        output.getFD().sync();
                    }
                    stagedFiles.add(staged);
                    stagedMetadata.add(new V86OfflineImageCompressor.PreparedImage(
                            prepared.getDisplayName(), new byte[0], prepared.getInspection(),
                            prepared.getRecompressed()));
                }
                if (!java.util.Objects.equals(expectedSessionId,
                        v86Controller.current().getSessionId())) {
                    throw new IllegalStateException(getString(R.string.v86_session_changed));
                }
                for (int index = 0; index < stagedFiles.size(); index++) {
                    waitForV86OfflineQueueRoom();
                    V86OfflineImageCompressor.PreparedImage metadata = stagedMetadata.get(index);
                    byte[] bytes = readSmallFile(stagedFiles.get(index),
                            V86OfflineImageCompressor.HARD_LIMIT_BYTES);
                    V86OfflineImageCompressor.PreparedImage prepared =
                            new V86OfflineImageCompressor.PreparedImage(
                                    metadata.getDisplayName(), bytes, metadata.getInspection(),
                                    metadata.getRecompressed());
                    String error = v86Controller.enqueueOfflineImageError(
                            prepared, expectedSessionId);
                    if (error != null) throw new IllegalStateException(error);
                    queued++;
                    int progress = queued;
                    runOnUiThread(() -> status.setText(getString(
                            R.string.v86_offline_queued_progress, progress, images.size())));
                    Thread.sleep(200L);
                }
                runOnUiThread(() -> showBanner(getString(
                        R.string.v86_offline_queued_complete, images.size())));
            } catch (Throwable error) {
                int accepted = queued;
                runOnUiThread(() -> {
                    status.setText(accepted == 0
                            ? getString(R.string.v86_offline_preflight_failed, error.getMessage())
                            : getString(R.string.v86_offline_partial_failed,
                                    accepted, images.size(), error.getMessage()));
                    showBanner(getString(R.string.v86_action_failed, error.getMessage()));
                });
            } finally {
                for (File staged : stagedFiles) staged.delete();
                stagingDirectory.delete();
            }
        });
    }

    private byte[] readSmallFile(File file, int maximumBytes) throws Exception {
        if (!file.isFile() || file.length() > maximumBytes) {
            throw new IllegalArgumentException(getString(R.string.v86_staged_file_invalid));
        }
        try (InputStream input = new java.io.FileInputStream(file);
             java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream(
                     (int) file.length())) {
            byte[] buffer = new byte[64 * 1024];
            int total = 0;
            while (true) {
                int count = input.read(buffer);
                if (count < 0) break;
                total += count;
                if (total > maximumBytes) {
                    throw new IllegalArgumentException(getString(R.string.v86_staged_file_invalid));
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    private void waitForV86OfflineQueueRoom() throws InterruptedException {
        long started = SystemClock.elapsedRealtime();
        while (v86Controller.current().getPendingCount() >= 12) {
            V86StreamingController.Snapshot state = v86Controller.current();
            if (state.getError() != null) throw new IllegalStateException(
                    getString(R.string.v86_upload_queue_paused, state.getError()));
            if (SystemClock.elapsedRealtime() - started >= 120_000L) {
                throw new IllegalStateException(getString(R.string.v86_queue_room_timeout));
            }
            Thread.sleep(200L);
        }
    }

    private void showSurveyPlanner(boolean visible) {
        surveyPlanningActive = visible;
        View panel = findViewById(R.id.survey_planner_panel);
        View summary = findViewById(R.id.survey_map_summary);
        panel.setVisibility(visible ? View.VISIBLE : View.GONE);
        summary.setVisibility(visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.survey_frame_all_button).setVisibility(
                visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.survey_map_3d_button).setVisibility(
                visible ? View.VISIBLE : View.GONE);
        if (visible) {
            if (!mapFullscreen) toggleMapFullscreen();
            summary.bringToFront();
            panel.bringToFront();
            showSurveyPlannerTab(surveyPlannerTab);
            renderSurveyOverlay();
            mapView.post(() -> {
                if (frameMapNearBestLiveLocation("survey entry", 18.0f, true, true)) return;
                if (surveyMission != null) frameSurveyMission(surveyMission);
                else if (!surveyRoi.isEmpty()) frameSurveyArea(surveyRoi);
                phoneMapLocationResolved = false;
                frameMapNearPhone();
            });
            renderSurveyStatus(surveyMission == null
                    ? getString(R.string.survey_planner_add_boundary_wgs84)
                    : surveySummary(surveyMission));
        } else {
            stopSurveyReplay(true);
            if (mapFullscreen) toggleMapFullscreen();
            appendLog("SURVEY planner panel closed · execution unchanged");
        }
    }

    private void toggleSurveyReplay() {
        if (surveyMission == null) {
            renderSurveyStatus(getString(R.string.survey_replay_requires_route));
            return;
        }
        if (surveyReplay == null || !surveyReplay.getMission().getId().equals(surveyMission.getId())) {
            surveyReplay = new SurveyMissionReplay(surveyMission, 2.0);
        }
        SurveyReplayState state = surveyReplay.getState();
        if (state == SurveyReplayState.RUNNING) {
            mainHandler.removeCallbacks(surveyReplayRunnable);
            renderSurveyReplay(surveyReplay.pause());
            appendLog("SURVEY preview paused · NO_CONTROL");
            return;
        }
        SurveyReplaySnapshot snapshot = state == SurveyReplayState.PAUSED
                ? surveyReplay.resume() : surveyReplay.start();
        renderSurveyReplay(snapshot);
        appendLog("SURVEY preview " + (state == SurveyReplayState.PAUSED ? "resumed" : "started")
                + " · NO_CONTROL");
        mainHandler.removeCallbacks(surveyReplayRunnable);
        mainHandler.postDelayed(surveyReplayRunnable, 80L);
    }

    private void stopSurveyReplay(boolean removeMarker) {
        mainHandler.removeCallbacks(surveyReplayRunnable);
        boolean hadReplay = surveyReplay != null;
        if (surveyReplay != null) surveyReplay.stop();
        surveyReplay = null;
        if (removeMarker && amap != null && surveyReplayMarker != null) {
            surveyReplayMarker.remove();
            surveyReplayMarker = null;
        }
        renderSurveyReplayButton(SurveyReplayState.IDLE);
        if (surveyPlanningActive && surveyMission != null) {
            renderSurveyStatus(getString(R.string.survey_replay_stopped, surveySummary(surveyMission)));
        }
        if (hadReplay) appendLog("SURVEY preview stopped · NO_CONTROL");
    }

    private void renderSurveyReplay(SurveyReplaySnapshot snapshot) {
        GeoPoint point = snapshot.getPoint();
        if (point != null && amap != null) {
            LatLng position = toMapLatLng(point);
            if (surveyReplayMarker == null) {
                surveyReplayMarker = (Marker) amap.addOverlay(new MarkerOptions()
                        .position(position)
                        .icon(surveyMarkerIcon("▶", 0xFF00A8C6))
                        .title("SURVEY PREVIEW · NO CONTROL"));
            } else {
                surveyReplayMarker.setPosition(position);
            }
        }
        renderSurveyReplayButton(snapshot.getState());
        int percent = (int) Math.round(snapshot.getProgress() * 100.0);
        if (snapshot.getState() == SurveyReplayState.COMPLETED) {
            renderSurveyStatus(getString(R.string.survey_replay_completed, surveySummary(surveyMission)));
            appendLog("SURVEY preview completed mission=" + surveyMission.getId());
        } else {
            String label = getString(snapshot.getState() == SurveyReplayState.PAUSED
                    ? R.string.survey_replay_paused : R.string.survey_replay_read_only);
            renderSurveyStatus(getString(R.string.survey_replay_progress,
                    label, percent, snapshot.getPassIndex() + 1,
                    getString(snapshot.getCaptureActive()
                            ? R.string.survey_replay_capturing : R.string.survey_replay_transition)));
        }
    }

    private void renderSurveyReplayButton(SurveyReplayState state) {
        Button replay = findViewById(R.id.survey_replay_button);
        if (replay == null) return;
        if (state == SurveyReplayState.RUNNING) replay.setText(getString(R.string.action_pause));
        else if (state == SurveyReplayState.PAUSED) replay.setText(getString(R.string.action_continue));
        else if (state == SurveyReplayState.COMPLETED) replay.setText(R.string.action_replay);
        else replay.setText(getString(R.string.preview));
    }

    private void updateSuggestedSurveyHeadingFromRoi() {
        if (surveyRoi.size() < 3) return;
        try {
            double heading = SurveyPlanner.INSTANCE.suggestedRouteHeading(
                    new ArrayList<>(surveyRoi));
            ((EditText) findViewById(R.id.survey_heading_input)).setText(String.format(
                    Locale.US, "%.1f", heading));
            schedulePersistSurveyPlannerSettings();
        } catch (IllegalArgumentException ignored) {
            // Keep the previous heading while the pilot is still editing an invalid polygon.
        }
    }

    private void generateSurveyMission(boolean obliqueFiveDirection) {
        if (surveyMission != null && surveyMission.getActiveMapping() != null) {
            showBanner(getString(R.string.recapture_regenerate_blocked));
            renderSurveyStatus(getString(R.string.recapture_preserve_exact_views));
            return;
        }
        if (rejectSurveyEditingIfLocked()) return;
        abortSurveySimulatorExecution(getString(R.string.reason_regenerate_survey_mission), true);
        stopSurveyReplay(true);
        if (surveyRoi.size() < 3) {
            renderSurveyStatus(getString(R.string.survey_generate_needs_boundary_status));
            showSurveyGenerationFailure(getString(R.string.survey_generate_needs_boundary_detail));
            return;
        }
        double altitude;
        double heading;
        double forwardOverlap;
        double sideOverlap;
        double speed;
        double obliqueSpeed;
        double gimbal;
        double margin;
        double targetOffset;
        double safeTakeoffAltitude;
        double takeoffSpeed;
        double obliqueForwardOverlap;
        double obliqueSideOverlap;
        double timedCaptureInterval;
        try {
            altitude = parseNumber((EditText) findViewById(R.id.survey_altitude_input));
            heading = parseNumber((EditText) findViewById(R.id.survey_heading_input));
            forwardOverlap = parseNumber((EditText) findViewById(R.id.survey_forward_overlap_input));
            sideOverlap = parseNumber((EditText) findViewById(R.id.survey_side_overlap_input));
            speed = parseNumber((EditText) findViewById(R.id.survey_speed_input));
            obliqueSpeed = parseNumber((EditText) findViewById(R.id.survey_oblique_speed_input));
            gimbal = parseNumber((EditText) findViewById(R.id.survey_gimbal_input));
            margin = parseNumber((EditText) findViewById(R.id.survey_margin_input));
            targetOffset = parseNumber((EditText) findViewById(R.id.survey_target_offset_input));
            safeTakeoffAltitude = parseNumber((EditText) findViewById(R.id.survey_safe_takeoff_input));
            takeoffSpeed = parseNumber((EditText) findViewById(R.id.survey_takeoff_speed_input));
            obliqueForwardOverlap = parseNumber((EditText) findViewById(
                    R.id.survey_oblique_forward_overlap_input));
            obliqueSideOverlap = parseNumber((EditText) findViewById(
                    R.id.survey_oblique_side_overlap_input));
            timedCaptureInterval = parseNumber((EditText) findViewById(
                    R.id.survey_timed_interval_input));
        } catch (IllegalArgumentException error) {
            renderSurveyStatus(getString(R.string.survey_generate_invalid_parameters_status));
            showSurveyGenerationFailure(getString(R.string.survey_generate_invalid_parameters_detail));
            return;
        }
        if (obliqueFiveDirection && Math.abs(gimbal + 90.0) < 0.001) {
            gimbal = -45.0;
            ((EditText) findViewById(R.id.survey_gimbal_input)).setText("-45");
            renderSurveyObliqueAngleButton();
            appendLog("SURVEY five-direction changed nadir -90deg to recommended -45deg");
        } else if (obliqueFiveDirection && (gimbal < -80.0 || gimbal > -30.0)) {
            showSurveyPlannerTab(SURVEY_TAB_CAPTURE);
            EditText input = findViewById(R.id.survey_gimbal_input);
            input.requestFocus();
            input.selectAll();
            renderSurveyStatus(getString(R.string.survey_oblique_pitch_range_status));
            showSurveyGenerationFailure(getString(R.string.survey_oblique_pitch_range_detail));
            return;
        }
        if (surveyTerrainFollowingEnabled && surveyTerrain == null) {
            if (obliqueFiveDirection && surveyMission != null
                    && surveyMission.getTerrainPlan() != null
                    && surveyMission.getConstraints().getCollectionMode()
                    == SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION) {
                try {
                    SurveyMission selectedMission = SurveyMissionCaptureViewFilter.select(
                            surveyMission, new HashSet<>(surveyEnabledCaptureViews), this);
                    completeGeneratedSurveyMission(selectedMission, null, surveyTerrainPreviewData);
                    appendLog("SURVEY reused terrain-adjusted waypoints groups="
                            + surveyCaptureViewSelectionLabel());
                } catch (IllegalArgumentException error) {
                    failSurveyGeneration(error);
                }
                return;
            }
            showMissingSurveyTerrainDialog(obliqueFiveDirection);
            return;
        }
        try {
            SurveyConstraints constraints = SurveyParameterPolicy.INSTANCE.createConstraints(
                    altitude, heading, forwardOverlap, sideOverlap, speed, gimbal, margin,
                    obliqueFiveDirection, targetOffset, safeTakeoffAltitude, takeoffSpeed,
                    obliqueForwardOverlap, obliqueSideOverlap,
                    selectedSurveyAltitudeMode(), selectedSurveyStartMode(),
                    selectedSurveyCompletionAction(), selectedSurveyCaptureMode(),
                    timedCaptureInterval, selectedSurveyTakeoffMode(),
                    new HashSet<>(surveyEnabledCaptureViews),
                    selectedSurveyObliqueHeadingMode(), obliqueSpeed, this);
            String missionName = ((EditText) findViewById(R.id.survey_name_input))
                    .getText().toString().trim();
            if (missionName.isEmpty()) missionName = getString(R.string.unnamed_area_route);
            SurveyMission plannedMission = SurveyPlanner.INSTANCE.plan(
                    missionName, new ArrayList<>(surveyRoi),
                    currentSurveyCameraProfile(), constraints,
                    currentSurveyPlanningTakeoffPoint());
            if (surveyTerrainFollowingEnabled && surveyTerrain != null) {
                boolean buildingDsm = ((Spinner) findViewById(R.id.survey_terrain_kind_spinner))
                        .getSelectedItemPosition() == 0;
                if (buildingDsm && !((CheckBox) findViewById(
                        R.id.survey_dsm_building_confirm)).isChecked()) {
                    throw new IllegalArgumentException(getString(R.string.verify_dsm_buildings_alignment_first));
                }
                final SurveyTerrainTakeoffReference takeoffReference =
                        currentSurveyTerrainTakeoffReference();
                if (takeoffReference == null) {
                    throw new IllegalArgumentException(getString(R.string.dsm_requires_home_gps));
                }
                final GeoPoint takeoffPoint = takeoffReference.getPoint();
                final SurveyMission baseMission = plannedMission;
                final TerrainElevationSource terrain = surveyTerrain;
                final String terrainSha256 = surveyTerrainSha256;
                final List<GeoPoint> roiSnapshot = new ArrayList<>(surveyRoi);
                surveyTerrainCalculationInFlight = true;
                renderSurveyStatus(getString(R.string.survey_terrain_calculating));
                storageExecutor.execute(() -> {
                    try {
                        TerrainImportSafety.INSTANCE.requireCompleteCoverage(
                                terrain, roiSnapshot, this);
                        SurveyTerrainPlanResult terrainResult = SurveyTerrainPlanner.INSTANCE.apply(
                                baseMission, terrain, takeoffPoint, terrainSha256,
                                SurveyTerrainPlanner.DEFAULT_SAMPLE_SPACING_METERS, this,
                                takeoffReference);
                        TerrainPreviewData focusedPreview = TerrainPreviewSampler.INSTANCE.forArea(
                                terrain, roiSnapshot, 96, 64, this);
                        runOnUiThread(() -> completeGeneratedSurveyMission(
                                terrainResult.getMission(), terrainResult.getSafety(), focusedPreview));
                    } catch (Throwable error) {
                        runOnUiThread(() -> failSurveyGeneration(error));
                    }
                });
                return;
            }
            completeGeneratedSurveyMission(plannedMission, null, null);
        } catch (IllegalArgumentException error) {
            failSurveyGeneration(error);
        }
    }

    private void showMissingSurveyTerrainDialog(boolean obliqueFiveDirection) {
        showBanner(getString(R.string.survey_terrain_missing_banner));
        appendLog("SURVEY generation blocked: terrain enabled but elevation source unavailable");
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_route_not_generated_title)
                .setMessage(R.string.survey_terrain_missing_message)
                .setPositiveButton(R.string.survey_generate_constant_altitude, (dialog, which) -> {
                    setSurveyTerrainFollowingEnabled(false, true);
                    generateSurveyMission(obliqueFiveDirection);
                })
                .setNegativeButton(R.string.survey_prepare_dsm, (dialog, which) -> {
                    showSurveyPlannerTab(SURVEY_TAB_TERRAIN);
                    renderSurveyStatus(getString(R.string.survey_prepare_elevation_status));
                })
                .setNeutralButton(R.string.action_cancel, null)
                .show();
    }

    private void completeGeneratedSurveyMission(SurveyMission mission,
            SurveyTerrainSafetyReport terrainSafety, TerrainPreviewData focusedPreview) {
        surveyTerrainCalculationInFlight = false;
        surveyMission = mission;
        if (mission.getActiveMapping() == null) {
            activeRecaptureSourceMission = null;
        } else if (activeRecaptureSourceMission == null
                || !activeRecaptureSourceContains(mission)) {
            activeRecaptureSourceMission = mission;
        }
        if (mission.getConstraints().getCollectionMode()
                != SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION) {
            surveyCaptureSelectionSourceMission = null;
        } else if (mission.getConstraints().getEnabledCaptureViews().size()
                == 5) {
            surveyCaptureSelectionSourceMission = mission;
        }
        if (focusedPreview != null) {
            surveyTerrainPreviewGrid = focusedPreview.getElevations();
            showSurveyTerrainPreview(focusedPreview);
        }
        persistSurveyMission(true);
        saveSurveyMissionVersion(false);
        renderSurveyOverlay();
        ((TerrainPreviewView) findViewById(R.id.survey_terrain_preview)).showMission(surveyMission);
        // Generation can finish after the planner drawer has already opened. In that path
        // showSurveyPlanner() had no mission to frame, so explicitly frame the newly-created
        // route once the map view has completed the current layout pass.
        View mapView = findViewById(R.id.map_view);
        if (mapView != null) {
            mapView.post(() -> {
                if (surveyMission == mission) frameSurveyMission(mission);
            });
        }
        String terrainLabel = terrainSafety == null ? "" : getString(
                    R.string.dsm_safety_summary,
                    terrainSafety.getControlPointCount(),
                    terrainSafety.getMinimumTerrainElevationMeters(),
                    terrainSafety.getMaximumTerrainElevationMeters(),
                    terrainSafety.getMinimumWaypointAltitudeMeters(),
                    terrainSafety.getMaximumWaypointAltitudeMeters(),
                    terrainSafety.getMaximumRequiredVerticalSpeedMetersPerSecond());
        renderSurveyStatus(surveySummary(surveyMission) + terrainLabel);
        appendLog("SURVEY generated " + surveySummary(surveyMission));
    }

    private void showSurveyTerrainPreview(TerrainPreviewData preview) {
        surveyTerrainPreviewData = preview;
        surveyTerrainPreviewGrid = preview.getElevations();
        ((TerrainPreviewView) findViewById(R.id.survey_terrain_preview)).showTerrain(
                preview.getInfo(), preview.getElevations(),
                preview.getColumns(), preview.getRows());
        renderSurveyTerrainMapOverlay();
    }

    private void clearSurveyTerrainPreview() {
        surveyTerrainPreviewData = null;
        surveyTerrainPreviewGrid = null;
        ((TerrainPreviewView) findViewById(R.id.survey_terrain_preview)).clearTerrain();
        renderSurveyTerrainMapOverlay();
    }

    /** Pilot 2 renders terrain-follow validation on the route itself, not as a raster card. */
    private void renderSurveyTerrainMapOverlay() {
        updateSurveyTerrainAltitudeLegend();
    }

    private void updateSurveyTerrainAltitudeLegend() {
        TerrainAltitudeLegendView legend = findViewById(R.id.survey_terrain_altitude_legend);
        if (legend == null) return;
        if (!surveyPlanningActive || surveyMission == null || surveyMission.getTerrainPlan() == null) {
            legend.clearRange();
            return;
        }
        double takeoffAsl = surveyMission.getTerrainPlan().getTakeoffTerrainElevationMeters();
        legend.showRange(
                takeoffAsl + surveyMission.getTerrainPlan().getMinimumWaypointAltitudeMeters(),
                takeoffAsl + surveyMission.getTerrainPlan().getMaximumWaypointAltitudeMeters());
    }

    private void failSurveyGeneration(Throwable error) {
        surveyTerrainCalculationInFlight = false;
        surveyMission = null;
        clearPersistedSurveySession();
        renderSurveyOverlay();
        renderSurveyStatus(getString(R.string.survey_generation_failed_status, error.getMessage()));
        appendLog("SURVEY generation failed " + error);
        showSurveyGenerationFailure(getString(R.string.survey_generation_failed_detail,
                error.getMessage()));
    }

    private void showSurveyGenerationFailure(String message) {
        showBanner(getString(R.string.survey_generation_failed_banner));
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_no_executable_route_title)
                .setMessage(message)
                .setPositiveButton(R.string.action_got_it, null)
                .show();
    }

    private String surveySummary(SurveyMission mission) {
        Set<Integer> passIndices = new HashSet<>();
        for (SurveyWaypoint waypoint : mission.getWaypoints()) passIndices.add(waypoint.getPassIndex());
        int passes = passIndices.size();
        double targetArea = SurveyPlanner.INSTANCE.targetArea(mission).getAreaSquareMeters();
        double coverageArea = SurveyPlanner.INSTANCE.groundCoverage(mission).getAreaSquareMeters();
        SurveyCollectionMode mode = mission.getConstraints().getCollectionMode();
        String modeLabel = mission.getActiveMapping() != null
                ? getString(R.string.active_recapture_summary,
                        mission.getActiveMapping().getSurveyCaptureCount(),
                        mission.getActiveMapping().getBridgeCaptureCount())
                : mode == SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION
                ? getString(R.string.five_direction_summary,
                        mission.getConstraints().getEnabledCaptureViews().size(),
                        surveyCaptureViewSelectionLabel(mission.getConstraints().getEnabledCaptureViews()))
                : getString(mode == SurveyCollectionMode.CROSSHATCH_NADIR
                        ? R.string.crosshatch_nadir : R.string.nadir_mapping);
        double gsd = SurveyPlanner.INSTANCE.coverage(
                mission.getCameraProfile(), mission.getConstraints()).getGroundSampleDistanceCentimeters();
        SurveyPlanner.CaptureFeasibility nadirCapture = SurveyPlanner.INSTANCE.captureFeasibility(
                mission.getCameraProfile(), mission.getConstraints(), false);
        SurveyPlanner.CaptureFeasibility obliqueCapture = SurveyPlanner.INSTANCE.captureFeasibility(
                mission.getCameraProfile(), mission.getConstraints(), true);
        boolean captureFeasible = nadirCapture.getFeasible()
                && (mode != SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION || obliqueCapture.getFeasible());
        String captureLabel = captureFeasible
                ? getString(R.string.survey_capture_interval_summary, nadirCapture.getMinimumIntervalSeconds())
                : getString(R.string.survey_capture_speed_warning,
                Math.min(nadirCapture.getMaximumFeasibleSpeedMetersPerSecond(),
                        obliqueCapture.getMaximumFeasibleSpeedMetersPerSecond()));
        String completionLabel = mission.getConstraints().getCompletionAction()
                == SurveyCompletionAction.RETURN_TO_HOME ? getString(R.string.survey_completion_rth)
                : mission.getConstraints().getCompletionAction() == SurveyCompletionAction.HOVER
                ? getString(R.string.survey_completion_hover) : getString(R.string.survey_completion_route_start);
        SurveyPlanner.MissionStatistics fullStatistics = SurveyPlanner.INSTANCE.statistics(
                mission, 5.0, 20.0 * 60.0, currentSurveyTakeoffPoint());
        String altitudeLabel = mission.getTerrainPlan() == null
                ? getString(R.string.survey_waypoint_altitude_summary, mission.getConstraints().getEffectiveFlightAltitudeMeters())
                : getString(R.string.survey_dsm_altitude_summary,
                mission.getTerrainPlan().getTargetAglMeters(),
                mission.getTerrainPlan().getMinimumWaypointAltitudeMeters(),
                mission.getTerrainPlan().getMaximumWaypointAltitudeMeters());
        return getString(R.string.survey_mission_summary,
                modeLabel,
                gsd, captureLabel, completionLabel,
                altitudeLabel, passes,
                targetArea, coverageArea, fullStatistics.getOperationalPathMeters(),
                mission.getEstimatedPhotoCount(), fullStatistics.getOperationalFlightSeconds() / 60.0);
    }

    private void setupSurveyOptionSpinners() {
        setupSurveySpinner(R.id.survey_altitude_mode_spinner,
                new String[]{getString(R.string.survey_altitude_surface), getString(R.string.survey_altitude_takeoff)});
        setupSurveySpinner(R.id.survey_start_mode_spinner,
                new String[]{getString(R.string.survey_start_nearest), getString(R.string.survey_corner_1),
                        getString(R.string.survey_corner_2), getString(R.string.survey_corner_3), getString(R.string.survey_corner_4)});
        setupSurveySpinner(R.id.survey_completion_spinner,
                new String[]{getString(R.string.survey_completion_rth), getString(R.string.survey_completion_hover),
                        getString(R.string.survey_completion_route_start)});
        setupSurveySpinner(R.id.survey_takeoff_mode_spinner,
                new String[]{getString(R.string.survey_takeoff_manual), getString(R.string.survey_takeoff_auto_sim)});
        setupSurveySpinner(R.id.survey_oblique_heading_mode_spinner,
                new String[]{getString(R.string.survey_heading_along_track), getString(R.string.survey_heading_fixed)});
        Spinner captureSpinner = setupSurveySpinner(R.id.survey_capture_mode_spinner,
                new String[]{getString(R.string.survey_capture_distance), getString(R.string.survey_capture_timed)});
        captureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                findViewById(R.id.survey_timed_interval_row).setVisibility(
                        position == 1 ? View.VISIBLE : View.GONE);
                updateSurveySpeedLimitHint();
                schedulePersistSurveyPlannerSettings();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        setupSurveySpeedLimitHint();
    }

    private int[] surveyPlannerTextInputIds() {
        return new int[] {
                R.id.survey_name_input,
                R.id.survey_altitude_input,
                R.id.survey_target_offset_input,
                R.id.survey_safe_takeoff_input,
                R.id.survey_heading_input,
                R.id.survey_takeoff_speed_input,
                R.id.survey_speed_input,
                R.id.survey_oblique_speed_input,
                R.id.survey_oblique_forward_overlap_input,
                R.id.survey_oblique_side_overlap_input,
                R.id.survey_timed_interval_input,
                R.id.survey_forward_overlap_input,
                R.id.survey_side_overlap_input,
                R.id.survey_margin_input,
                R.id.survey_gimbal_input,
        };
    }

    private int[] surveyPlannerSpinnerIds() {
        return new int[] {
                R.id.survey_takeoff_mode_spinner,
                R.id.survey_altitude_mode_spinner,
                R.id.survey_start_mode_spinner,
                R.id.survey_completion_spinner,
                R.id.survey_capture_mode_spinner,
                R.id.survey_oblique_heading_mode_spinner,
        };
    }

    private void setupSurveyPlannerSettingsPersistence() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                schedulePersistSurveyPlannerSettings();
                renderSurveyObliqueAngleButton();
            }
        };
        for (int id : surveyPlannerTextInputIds()) {
            ((EditText) findViewById(id)).addTextChangedListener(watcher);
        }
        for (int id : surveyPlannerSpinnerIds()) {
            Spinner spinner = findViewById(id);
            if (id == R.id.survey_capture_mode_spinner) continue;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(
                        AdapterView<?> parent, View view, int position, long itemId) {
                    schedulePersistSurveyPlannerSettings();
                    updateSurveySpeedLimitHint();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void schedulePersistSurveyPlannerSettings() {
        if (surveySettingsRestoring) return;
        mainHandler.removeCallbacks(persistSurveySettingsRunnable);
        mainHandler.postDelayed(persistSurveySettingsRunnable, 300L);
    }

    private void persistSurveyPlannerSettings() {
        if (surveySettingsRestoring) return;
        try {
            JSONObject root = new JSONObject()
                    .put("schema_version", SURVEY_SETTINGS_SCHEMA_VERSION)
                    .put("five_direction", surveySpeedHintFiveDirection)
                    .put("terrain_enabled", surveyTerrainFollowingEnabled);
            org.json.JSONArray captureViews = new org.json.JSONArray();
            for (SurveyCaptureView view : surveyEnabledCaptureViews) captureViews.put(view.name());
            root.put("enabled_capture_views", captureViews);
            JSONObject textInputs = new JSONObject();
            for (int id : surveyPlannerTextInputIds()) {
                textInputs.put(getResources().getResourceEntryName(id),
                        ((EditText) findViewById(id)).getText().toString());
            }
            JSONObject spinners = new JSONObject();
            for (int id : surveyPlannerSpinnerIds()) {
                spinners.put(getResources().getResourceEntryName(id),
                        ((Spinner) findViewById(id)).getSelectedItemPosition());
            }
            root.put("text_inputs", textInputs).put("spinners", spinners);
            getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                    .putString(SURVEY_SETTINGS_KEY, root.toString()).apply();
        } catch (Throwable error) {
            appendLog("SURVEY settings save failed: " + error.getMessage());
        }
    }

    private void restoreSurveyPlannerSettings() {
        String raw = getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE)
                .getString(SURVEY_SETTINGS_KEY, null);
        if (raw == null) return;
        try {
            JSONObject root = new JSONObject(raw);
            if (root.getInt("schema_version") != SURVEY_SETTINGS_SCHEMA_VERSION) {
                throw new IllegalArgumentException("unsupported planner settings schema");
            }
            surveySpeedHintFiveDirection = root.optBoolean("five_direction", false);
            surveyTerrainFollowingEnabled = root.optBoolean("terrain_enabled", false);
            surveyEnabledCaptureViews.clear();
            org.json.JSONArray captureViews = root.optJSONArray("enabled_capture_views");
            if (captureViews == null) {
                surveyEnabledCaptureViews.addAll(EnumSet.of(SurveyCaptureView.NADIR,
                        SurveyCaptureView.FORWARD_OBLIQUE, SurveyCaptureView.BACKWARD_OBLIQUE,
                        SurveyCaptureView.LEFT_OBLIQUE, SurveyCaptureView.RIGHT_OBLIQUE));
            } else {
                for (int index = 0; index < captureViews.length(); index++) {
                    surveyEnabledCaptureViews.add(SurveyCaptureView.valueOf(
                            captureViews.getString(index)));
                }
                if (surveyEnabledCaptureViews.isEmpty()) {
                    surveyEnabledCaptureViews.addAll(EnumSet.of(SurveyCaptureView.NADIR,
                            SurveyCaptureView.FORWARD_OBLIQUE, SurveyCaptureView.BACKWARD_OBLIQUE,
                            SurveyCaptureView.LEFT_OBLIQUE, SurveyCaptureView.RIGHT_OBLIQUE));
                }
            }
            ((CheckBox) findViewById(R.id.survey_terrain_enabled_checkbox))
                    .setChecked(surveyTerrainFollowingEnabled);
            JSONObject textInputs = root.getJSONObject("text_inputs");
            for (int id : surveyPlannerTextInputIds()) {
                String key = getResources().getResourceEntryName(id);
                if (textInputs.has(key)) {
                    ((EditText) findViewById(id)).setText(textInputs.getString(key));
                }
            }
            JSONObject spinners = root.getJSONObject("spinners");
            for (int id : surveyPlannerSpinnerIds()) {
                String key = getResources().getResourceEntryName(id);
                if (!spinners.has(key)) continue;
                Spinner spinner = findViewById(id);
                int position = spinners.getInt(key);
                if (position >= 0 && position < spinner.getCount()) spinner.setSelection(position);
            }
            findViewById(R.id.survey_timed_interval_row).setVisibility(
                    selectedSurveyCaptureMode() == SurveyCaptureTriggerMode.TIME
                            ? View.VISIBLE : View.GONE);
            updateSurveySpeedLimitHint();
            renderSurveyObliqueAngleButton();
            renderSurveyRoutePreviewStyles();
            renderSurveyTerrainModeControl();
            appendLog("SURVEY planner settings restored");
        } catch (Throwable error) {
            getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                    .remove(SURVEY_SETTINGS_KEY).apply();
            appendLog("SURVEY settings ignored invalid data: " + error.getMessage());
        }
    }

    private void setupSurveySpeedLimitHint() {
        int[] inputs = new int[] {
                R.id.survey_altitude_input, R.id.survey_speed_input,
                R.id.survey_oblique_speed_input,
                R.id.survey_takeoff_speed_input, R.id.survey_forward_overlap_input,
                R.id.survey_side_overlap_input, R.id.survey_oblique_forward_overlap_input,
                R.id.survey_oblique_side_overlap_input, R.id.survey_gimbal_input,
                R.id.survey_timed_interval_input
        };
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateSurveySpeedLimitHint(); }
        };
        for (int id : inputs) ((EditText) findViewById(id)).addTextChangedListener(watcher);
        updateSurveySpeedLimitHint();
    }

    private void setupSurveyImeActions() {
        int[] inputs = new int[] {
                R.id.survey_name_input, R.id.survey_altitude_input, R.id.survey_gsd_input,
                R.id.survey_target_offset_input, R.id.survey_safe_takeoff_input,
                R.id.survey_rth_height_input, R.id.survey_heading_input,
                R.id.survey_takeoff_speed_input, R.id.survey_speed_input,
                R.id.survey_oblique_speed_input,
                R.id.survey_oblique_forward_overlap_input, R.id.survey_oblique_side_overlap_input,
                R.id.survey_timed_interval_input, R.id.survey_forward_overlap_input,
                R.id.survey_side_overlap_input, R.id.survey_gimbal_input,
                R.id.survey_margin_input
        };
        for (int id : inputs) {
            EditText input = findViewById(id);
            if (input == null) continue;
            input.setSingleLine(true);
            input.setImeOptions(EditorInfo.IME_ACTION_DONE);
            input.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId != EditorInfo.IME_ACTION_DONE) return false;
                view.clearFocus();
                InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (keyboard != null) keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                schedulePersistSurveyPlannerSettings();
                updateSurveySpeedLimitHint();
                return true;
            });
        }
    }

    private void updateSurveySpeedLimitHint() {
        TextView hint = findViewById(R.id.survey_speed_limit_hint);
        EditText speedInput = findViewById(R.id.survey_speed_input);
        EditText obliqueSpeedInput = findViewById(R.id.survey_oblique_speed_input);
        EditText takeoffInput = findViewById(R.id.survey_takeoff_speed_input);
        if (hint == null || speedInput == null || obliqueSpeedInput == null || takeoffInput == null) return;
        speedInput.setError(null);
        obliqueSpeedInput.setError(null);
        takeoffInput.setError(null);
        try {
            double altitude = parseNumber((EditText) findViewById(R.id.survey_altitude_input));
            double speed = parseNumber(speedInput);
            double obliqueSpeed = parseNumber(obliqueSpeedInput);
            double takeoffSpeed = parseNumber(takeoffInput);
            double gimbal = parseNumber((EditText) findViewById(R.id.survey_gimbal_input));
            double timedInterval = parseNumber((EditText) findViewById(R.id.survey_timed_interval_input));
            boolean fiveDirection = surveySpeedHintFiveDirection;
            if (surveyMission != null) {
                fiveDirection = surveyMission.getConstraints().getCollectionMode()
                        == SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION;
            }
            if (speed > 10.0) {
                speedInput.setError(getString(R.string.survey_nadir_speed_hard_limit));
            }
            if (obliqueSpeed > 10.0) {
                obliqueSpeedInput.setError(getString(R.string.survey_oblique_speed_hard_limit));
            }
            if (takeoffSpeed > 10.0) {
                takeoffInput.setError(getString(R.string.survey_climb_speed_hard_limit));
            }
            // Clamp only the temporary object used for limit computation. Generation still
            // rejects the original value, so no requested speed is silently changed.
            double speedForLimit = Math.max(0.5, Math.min(10.0, speed));
            double obliqueSpeedForLimit = Math.max(0.5, Math.min(10.0, obliqueSpeed));
            double takeoffSpeedForLimit = Math.max(0.5, Math.min(10.0, takeoffSpeed));
            SurveyConstraints constraints = SurveyParameterPolicy.INSTANCE.createConstraints(
                    altitude, 0.0,
                    parseNumber((EditText) findViewById(R.id.survey_forward_overlap_input)),
                    parseNumber((EditText) findViewById(R.id.survey_side_overlap_input)),
                    speedForLimit, gimbal, 0.0, fiveDirection, 0.0, 30.0,
                    takeoffSpeedForLimit,
                    parseNumber((EditText) findViewById(R.id.survey_oblique_forward_overlap_input)),
                    parseNumber((EditText) findViewById(R.id.survey_oblique_side_overlap_input)),
                    SurveyAltitudeMode.ABOVE_TARGET_SURFACE, SurveyStartPointMode.AUTO_NEAREST,
                    SurveyCompletionAction.RETURN_TO_HOME, selectedSurveyCaptureMode(), timedInterval,
                    selectedSurveyTakeoffMode(), new HashSet<>(surveyEnabledCaptureViews),
                    selectedSurveyObliqueHeadingMode(), obliqueSpeedForLimit, this);
            SurveyPlanner.SpeedLimit limit = SurveyPlanner.INSTANCE.speedLimit(
                    currentSurveyCameraProfile(), constraints);
            boolean speedExceeded = limit.getExceeded();
            boolean takeoffExceeded = takeoffSpeed > 10.0;
            if (speedExceeded) {
                SurveyPlanner.CaptureFeasibility nadir = SurveyPlanner.INSTANCE.captureFeasibility(
                        currentSurveyCameraProfile(), constraints, false);
                SurveyPlanner.CaptureFeasibility oblique = SurveyPlanner.INSTANCE.captureFeasibility(
                        currentSurveyCameraProfile(), constraints, true);
                if (speed > Math.min(10.0, nadir.getMaximumFeasibleSpeedMetersPerSecond())) {
                    speedInput.setError(getString(R.string.survey_exceeds_nadir_limit,
                            Math.min(10.0, nadir.getMaximumFeasibleSpeedMetersPerSecond())));
                }
                if (obliqueSpeed > Math.min(10.0, oblique.getMaximumFeasibleSpeedMetersPerSecond())) {
                    obliqueSpeedInput.setError(getString(R.string.survey_exceeds_oblique_limit,
                            Math.min(10.0, oblique.getMaximumFeasibleSpeedMetersPerSecond())));
                }
            }
            if (takeoffExceeded) takeoffInput.setError(getString(R.string.survey_climb_speed_hard_limit));
            if (limit.getCameraLimited()) {
                hint.setText(getString(R.string.survey_dynamic_speed_limits,
                        limit.getEffectiveMaximumMetersPerSecond(),
                        speedExceeded || takeoffExceeded
                                ? getString(R.string.survey_reduce_speed_warning_suffix) : ""));
            } else {
                hint.setText(getString(R.string.survey_speed_unlimited_mode,
                        speedExceeded || takeoffExceeded
                                ? getString(R.string.survey_reduce_speed_warning) : ""));
            }
            hint.setTextColor(getColor(speedExceeded || takeoffExceeded
                    ? R.color.text_danger : R.color.text_muted));
        } catch (RuntimeException error) {
            hint.setText(R.string.survey_speed_parameters_incomplete);
            hint.setTextColor(getColor(R.color.text_muted));
        }
    }

    private Spinner setupSurveySpinner(int viewId, String[] labels) {
        Spinner spinner = findViewById(viewId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, labels) {
            private View styleSpinnerText(View view, boolean dropdown) {
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextColor(0xFF30353B);
                    text.setTextSize(dropdown ? 12f : 10f);
                    text.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                    text.setPadding(dp(8), 0, dp(8), 0);
                    if (dropdown) text.setBackgroundColor(0xFFFFFFFF);
                }
                return view;
            }

            @Override public View getView(int position, View convertView, ViewGroup parent) {
                return styleSpinnerText(super.getView(position, convertView, parent), false);
            }

            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return styleSpinnerText(super.getDropDownView(position, convertView, parent), true);
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private SurveyAltitudeMode selectedSurveyAltitudeMode() {
        return ((Spinner) findViewById(R.id.survey_altitude_mode_spinner)).getSelectedItemPosition() == 1
                ? SurveyAltitudeMode.RELATIVE_TO_TAKEOFF : SurveyAltitudeMode.ABOVE_TARGET_SURFACE;
    }

    private SurveyStartPointMode selectedSurveyStartMode() {
        switch (((Spinner) findViewById(R.id.survey_start_mode_spinner)).getSelectedItemPosition()) {
            case 1: return SurveyStartPointMode.FIRST_ROUTE_START;
            case 2: return SurveyStartPointMode.ROUTE_CORNER_2;
            case 3: return SurveyStartPointMode.ROUTE_CORNER_3;
            case 4: return SurveyStartPointMode.ROUTE_CORNER_4;
            default: return SurveyStartPointMode.AUTO_NEAREST;
        }
    }

    private SurveyCompletionAction selectedSurveyCompletionAction() {
        int position = ((Spinner) findViewById(R.id.survey_completion_spinner)).getSelectedItemPosition();
        return position == 1 ? SurveyCompletionAction.HOVER
                : position == 2 ? SurveyCompletionAction.RETURN_TO_ROUTE_START
                : SurveyCompletionAction.RETURN_TO_HOME;
    }

    private SurveyCaptureTriggerMode selectedSurveyCaptureMode() {
        return ((Spinner) findViewById(R.id.survey_capture_mode_spinner)).getSelectedItemPosition() == 1
                ? SurveyCaptureTriggerMode.TIME : SurveyCaptureTriggerMode.DISTANCE;
    }

    private SurveyTakeoffMode selectedSurveyTakeoffMode() {
        return ((Spinner) findViewById(R.id.survey_takeoff_mode_spinner)).getSelectedItemPosition() == 1
                ? SurveyTakeoffMode.AUTO_SIMULATOR_ONLY : SurveyTakeoffMode.MANUAL;
    }

    private SurveyObliqueHeadingMode selectedSurveyObliqueHeadingMode() {
        return ((Spinner) findViewById(R.id.survey_oblique_heading_mode_spinner))
                .getSelectedItemPosition() == 1
                ? SurveyObliqueHeadingMode.FIXED_CAPTURE_DIRECTION
                : SurveyObliqueHeadingMode.TRACK_ROUTE;
    }

    private GeoPoint currentSurveyTakeoffPoint() {
        if (Double.isFinite(aircraftSnapshot.getHomeLatitude())
                && Double.isFinite(aircraftSnapshot.getHomeLongitude())) {
            return new GeoPoint(aircraftSnapshot.getHomeLatitude(),
                    aircraftSnapshot.getHomeLongitude(), 0.0);
        }
        GeoPoint aircraft = effectiveAircraftGeoPoint(aircraftSnapshot);
        return aircraft == null ? null : new GeoPoint(
                aircraft.getLatitude(), aircraft.getLongitude(), 0.0);
    }

    private GeoPoint currentSurveyPlanningTakeoffPoint() {
        GeoPoint aircraftTakeoff = currentSurveyTakeoffPoint();
        if (aircraftTakeoff != null) return aircraftTakeoff;
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0
                ? surveyDebugPreviewTakeoffPoint : null;
    }

    private SurveyTerrainTakeoffReference currentSurveyTerrainTakeoffReference() {
        if (!aircraftSnapshot.getConnected()) return null;
        long capturedAt = System.currentTimeMillis();
        if (aircraftSnapshot.getHomeLocationSet()
                && Double.isFinite(aircraftSnapshot.getHomeLatitude())
                && Double.isFinite(aircraftSnapshot.getHomeLongitude())) {
            return new SurveyTerrainTakeoffReference(
                    new GeoPoint(aircraftSnapshot.getHomeLatitude(),
                            aircraftSnapshot.getHomeLongitude(), 0.0),
                    SurveyTerrainTakeoffReferenceSource.HOME_LOCATION,
                    capturedAt);
        }
        if (Double.isFinite(aircraftSnapshot.getLatitude())
                && Double.isFinite(aircraftSnapshot.getLongitude())) {
            return new SurveyTerrainTakeoffReference(
                    new GeoPoint(aircraftSnapshot.getLatitude(),
                            aircraftSnapshot.getLongitude(), 0.0),
                    SurveyTerrainTakeoffReferenceSource.AIRCRAFT_LOCATION,
                    capturedAt);
        }
        return null;
    }

    private GeoPoint currentAircraftGeoPoint() {
        return effectiveAircraftGeoPoint(aircraftSnapshot);
    }

    /** Simulator-only return target: true Home horizontally, takeoff-hover height vertically. */
    private GeoPoint currentSurveySimulatorReturnPoint() {
        if (Double.isFinite(aircraftSnapshot.getHomeLatitude())
                && Double.isFinite(aircraftSnapshot.getHomeLongitude())) {
            return new GeoPoint(aircraftSnapshot.getHomeLatitude(),
                    aircraftSnapshot.getHomeLongitude(), 1.2);
        }
        GeoPoint current = currentAircraftGeoPoint();
        return current == null ? null : new GeoPoint(
                current.getLatitude(), current.getLongitude(), 1.2);
    }

    private void setupSurveyGsdAltitudeLink() {
        EditText altitudeInput = findViewById(R.id.survey_altitude_input);
        EditText gsdInput = findViewById(R.id.survey_gsd_input);
        TextWatcher altitudeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                if (surveyGsdLinkUpdating) return;
                try {
                    double altitude = Double.parseDouble(editable.toString());
                    SurveyConstraints constraints = new SurveyConstraints(
                            altitude, 0.80, 0.70, 3.0, 3.0, -90.0, 0.0, false,
                            SurveyCollectionMode.ORTHO, -45.0, 0.0,
                            edu.playground.djivln.survey.SurveyAltitudeMode.ABOVE_TARGET_SURFACE,
                            0.0, 30.0, 3.0, 2.0,
                            SurveyTakeoffMode.MANUAL,
                            edu.playground.djivln.survey.SurveyStartPointMode.AUTO_NEAREST,
                            edu.playground.djivln.survey.SurveyCompletionAction.RETURN_TO_HOME,
                            edu.playground.djivln.survey.SurveyCaptureTriggerMode.DISTANCE,
                            2.0, 0.80, 0.70,
                            SurveyObliqueHeadingMode.TRACK_ROUTE,
                            EnumSet.allOf(SurveyCaptureView.class));
                    double gsd = SurveyPlanner.INSTANCE.coverage(
                            currentSurveyCameraProfile(), constraints)
                            .getGroundSampleDistanceCentimeters();
                    surveyGsdLinkUpdating = true;
                    gsdInput.setText(String.format(Locale.US, "%.2f", gsd));
                } catch (RuntimeException ignored) {
                    // Keep partial numeric input editable; generation performs strict validation.
                } finally {
                    surveyGsdLinkUpdating = false;
                }
            }
        };
        TextWatcher gsdWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                if (surveyGsdLinkUpdating) return;
                try {
                    double gsd = Double.parseDouble(editable.toString());
                    double altitude = SurveyPlanner.INSTANCE.altitudeForGroundSampleDistance(
                            currentSurveyCameraProfile(), gsd);
                    surveyGsdLinkUpdating = true;
                    altitudeInput.setText(String.format(Locale.US, "%.1f", altitude));
                } catch (RuntimeException ignored) {
                    // Keep partial numeric input editable; generation performs strict validation.
                } finally {
                    surveyGsdLinkUpdating = false;
                }
            }
        };
        altitudeInput.addTextChangedListener(altitudeWatcher);
        gsdInput.addTextChangedListener(gsdWatcher);
        altitudeWatcher.afterTextChanged(altitudeInput.getText());
    }

    private CameraProfile currentSurveyCameraProfile() {
        return aircraftBridge == null
                ? CameraProfile.Companion.getGENERIC_4_BY_3()
                : aircraftBridge.currentSurveyCameraProfile();
    }

    private void runSurveySimulatorGateCheck() {
        if (surveyMission == null) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_missing_route), true);
            appendLog("SURVEY simulator gate blocked: mission missing");
            return;
        }
        if (mockUiActive && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_debug_passed), false);
            appendLog("SURVEY UI DRY RUN gate allowed · NO_CONTROL · NO_CAMERA");
            return;
        }
        SurveyExecutionGateResult gate = currentSurveySimulatorGate(false);
        if (gate.getAllowed()) {
            String environment = aircraftSnapshot.getSimulatorActive()
                    ? "SIMULATOR" : getString(R.string.survey_gate_real_manual);
            setSurveySimulatorStatus(getString(R.string.survey_gate_passed,
                    gate.getStartDistanceMeters(), environment), false);
            appendLog(String.format(Locale.US,
                    "SURVEY simulator gate allowed start=%.1fm · NO_CONTROL", gate.getStartDistanceMeters()));
        } else {
            StringBuilder reasons = new StringBuilder();
            for (SurveyExecutionBlock block : gate.getBlocks()) {
                if (reasons.length() > 0) reasons.append(getString(R.string.list_separator));
                reasons.append(surveyBlockLabel(block));
            }
            setSurveySimulatorStatus(getString(R.string.survey_gate_blocked_reasons,
                    reasons.toString()), true);
            appendLog("SURVEY simulator gate blocked: " + gate.getBlocks() + " · NO_CONTROL");
        }
    }

    private SurveyExecutionGateResult currentSurveySimulatorGate(boolean requireVirtualStick) {
        return currentSurveySimulatorGate(requireVirtualStick, false, true);
    }

    private SurveyExecutionGateResult currentSurveySimulatorGate(
            boolean requireVirtualStick, boolean allowNotFlying) {
        return currentSurveySimulatorGate(requireVirtualStick, allowNotFlying, true);
    }

    private SurveyExecutionGateResult currentSurveySimulatorGate(
            boolean requireVirtualStick, boolean allowNotFlying, boolean checkPreflightReadiness) {
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        SurveyExecutionEnvironment environment = surveyExecutionEnvironment;
        if (environment == null || surveySimulatorExecution == null
                || (surveySimulatorExecution.getStatus().getState() != SurveyExecutionState.ARMING
                && surveySimulatorExecution.getStatus().getState() != SurveyExecutionState.RUNNING
                && surveySimulatorExecution.getStatus().getState() != SurveyExecutionState.PAUSED)) {
            environment = snapshot.getSimulatorActive()
                    ? SurveyExecutionEnvironment.DJI_SIMULATOR
                    : SurveyExecutionEnvironment.REAL_AIRCRAFT_MANUAL_TAKEOFF;
        }
        SurveyExecutionTelemetry telemetry = new SurveyExecutionTelemetry(
                snapshot.getConnected(),
                snapshot.getSimulatorActive(),
                snapshot.getSimulatorFlying(),
                snapshot.getVirtualStickEnabled(),
                snapshot.getSticksActive(),
                snapshot.getLatitude(),
                snapshot.getLongitude(),
                snapshot.getAltitude(),
                snapshot.getFlightStateUpdatedAtMs(),
                snapshot.getFlying(), snapshot.getAircraftBattery(), snapshot.getRcBattery(),
                snapshot.getRcSignal(), snapshot.getSatellites(),
                "LEVEL_4".equals(snapshot.getGpsLevel()) || "LEVEL_5".equals(snapshot.getGpsLevel()),
                Double.isFinite(snapshot.getHomeLatitude()) && Double.isFinite(snapshot.getHomeLongitude()),
                snapshot.getHomeLatitude(), snapshot.getHomeLongitude(),
                snapshot.getGoHomeHeightMeters(), snapshot.getMaxFlightHeightMeters(),
                snapshot.getMaxFlightRadiusMeters(), snapshot.getMaxFlightRadiusEnabled(),
                snapshot.getHorizontalSpeed(), snapshot.getVerticalSpeed(),
                snapshot.getGoingHome(), snapshot.getLanding());
        return SurveySimulatorGate.INSTANCE.evaluate(
                surveyMission, telemetry, System.currentTimeMillis(), requireVirtualStick,
                allowNotFlying, allowNotFlying, environment, checkPreflightReadiness);
    }

    private String surveyBlockLabel(SurveyExecutionBlock block) {
        switch (block) {
            case AIRCRAFT_DISCONNECTED: return getString(R.string.flight_controller_disconnected);
            case SIMULATOR_REQUIRED: return getString(R.string.gate_simulator_required);
            case SIMULATOR_NOT_FLYING: return getString(R.string.gate_simulator_not_flying);
            case SIMULATOR_MUST_BE_OFF: return getString(R.string.gate_simulator_must_be_off);
            case REAL_AIRCRAFT_NOT_FLYING: return getString(R.string.gate_manual_takeoff_hover_first);
            case REAL_AIRCRAFT_NOT_STABLY_HOVERING: return getString(R.string.gate_stable_hover_required);
            case REAL_REQUIRES_MANUAL_TAKEOFF: return getString(R.string.gate_real_manual_takeoff_only);
            case TELEMETRY_STALE: return getString(R.string.gate_telemetry_stale);
            case GPS_UNAVAILABLE: return getString(R.string.gate_gps_unavailable);
            case MANUAL_TAKEOVER: return getString(R.string.gate_manual_takeover);
            case VIRTUAL_STICK_REQUIRED: return getString(R.string.gate_vs_not_ready);
            case UNSUPPORTED_COORDINATE_FRAME: return getString(R.string.gate_coordinate_frame_invalid);
            case MISSION_TOO_LONG: return getString(R.string.gate_mission_too_long);
            case MISSION_ALTITUDE_UNSAFE: return getString(R.string.gate_mission_altitude_unsafe);
            case CAMERA_TRIGGER_UNSAFE: return getString(R.string.gate_camera_trigger_unsafe);
            case AIRCRAFT_BATTERY_LOW: return getString(R.string.gate_aircraft_battery_low);
            case RC_BATTERY_LOW: return getString(R.string.gate_rc_battery_low);
            case RC_SIGNAL_WEAK: return getString(R.string.gate_rc_signal_weak);
            case GPS_SATELLITES_LOW: return getString(R.string.gate_gps_satellites_low);
            case GPS_SIGNAL_WEAK: return getString(R.string.gate_gps_signal_weak);
            case HOME_LOCATION_REQUIRED: return getString(R.string.gate_home_invalid);
            case GO_HOME_HEIGHT_UNSAFE: return getString(R.string.gate_rth_height_unsafe);
            case MAX_FLIGHT_HEIGHT_TOO_LOW: return getString(R.string.gate_max_height_too_low);
            case MAX_FLIGHT_RADIUS_REQUIRED: return getString(R.string.gate_radius_invalid);
            case MAX_FLIGHT_RADIUS_TOO_SMALL: return getString(R.string.gate_radius_too_small);
            case FLIGHT_CONTROLLER_FAILSAFE_ACTIVE: return getString(R.string.gate_failsafe_active);
            case TERRAIN_REAL_FLIGHT_NOT_VERIFIED: return getString(R.string.gate_terrain_real_unverified);
            default: return block.name();
        }
    }

    private void startSurveySimulatorExecution() {
        if (surveyTerrainCalculationInFlight) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_terrain_calculating), true);
            return;
        }
        if (mockUiActive && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            startSurveyUiDryRun();
            return;
        }
        if (surveyMission == null || surveyMission.getWaypoints().isEmpty()) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_missing_valid_mission), true);
            appendLog("SURVEY execution blocked: mission missing · NO_CONTROL");
            return;
        }
        String terrainReferenceIssue = terrainTakeoffReferenceIssue();
        if (terrainReferenceIssue != null) {
            setSurveySimulatorStatus(terrainReferenceIssue, true);
            appendLog("SURVEY terrain takeoff reference blocked · " + terrainReferenceIssue);
            return;
        }
        String importedCameraIssue = importedMissionCameraIssue();
        if (importedCameraIssue != null) {
            setSurveySimulatorStatus(importedCameraIssue, true);
            appendLog("SURVEY imported camera compatibility blocked · " + importedCameraIssue);
            return;
        }
        boolean autoTakeoff = surveyMission.getConstraints().getTakeoffMode()
                == SurveyTakeoffMode.AUTO_SIMULATOR_ONLY;
        if (autoTakeoff && !aircraftSnapshot.getSimulatorActive()) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_real_auto_takeoff_forbidden), true);
            appendLog("SURVEY real execution blocked: simulator-only auto takeoff selected · NO_CONTROL");
            return;
        }
        if (autoTakeoff && !aircraftSnapshot.getSimulatorFlying()) {
            startSurveyAutoTakeoff();
            return;
        }
        boolean resuming = surveySimulatorExecution != null
                && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.PAUSED;
        SurveyExecutionGateResult armGate = resuming
                ? currentSurveySimulatorGate(false, false, false)
                : currentSurveySimulatorGate(false);
        if (!armGate.getAllowed()) {
            renderSurveySimulatorGateFailure(getString(resuming
                    ? R.string.temporarily_cannot_resume : R.string.start_blocked), armGate);
            appendLog("SURVEY execution " + (resuming ? "resume" : "arm")
                    + " blocked: " + armGate.getBlocks() + " · NO_CONTROL");
            return;
        }

        if (controlArmed || autoInferenceEnabled || chunkExecutionActive
                || activeRelativeMoveRunnable != null) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_controller_active), true);
            appendLog("SURVEY execution blocked: another controller active · NO_CONTROL");
            return;
        }
        if (aircraftSnapshot.getVirtualStickEnabled()) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_vs_owned), true);
            appendLog("SURVEY execution blocked: VS ownership unknown · NO_CONTROL");
            return;
        }
        if (!resuming) {
            surveyExecutionEnvironment = aircraftSnapshot.getSimulatorActive()
                    ? SurveyExecutionEnvironment.DJI_SIMULATOR
                    : SurveyExecutionEnvironment.REAL_AIRCRAFT_MANUAL_TAKEOFF;
            appendSurveyMissionStartLog(armGate);
            stopSurveyReplay(true);
            surveyCaptureController.configure(
                    surveyMission.getConstraints().getCaptureTriggerMode(),
                    surveyMission.getConstraints().getTimedCaptureIntervalSeconds());
            surveyGimbalCommandedPitch = Double.NaN;
            surveyGimbalSettlingStartedElapsedMs = 0L;
            surveyGimbalLastCommandElapsedMs = 0L;
            surveyGimbalCommandAcceptedElapsedMs = 0L;
            surveyGimbalVerificationStartedElapsedMs = 0L;
            surveyGimbalCommandGeneration++;
            surveyGimbalCommandAttempts = 0;
            surveyGimbalLimitActive = false;
            surveyPendingCaptureStart = null;
            surveyPointCapturePendingLegIndex = -1;
            surveyPointCaptureCompletedLegIndex = -1;
            aircraftBridge.prepareSurveyPhotoMode();
            invalidateSurveyPhotoRequest();
            surveySimulatorExecution = new SurveySimulatorExecutionStateMachine(
                    surveyMission, currentAircraftGeoPoint(), currentSurveySimulatorReturnPoint());
            SurveyExecutionStatus arming = surveySimulatorExecution.requestArm(armGate);
            if (arming.getState() != SurveyExecutionState.ARMING) {
                renderSurveySimulatorExecutionStatus(arming, null);
                return;
            }
            persistSurveyCheckpoint(arming);
            if (hilVirtualFramesEnabled) {
                ueBridgeEnabled = true;
                ((Button) findViewById(R.id.ue_bridge_toggle)).setText(R.string.hil_telemetry_mirror_on);
                sendSurveyMissionToUe();
            }
        }
        setSurveySimulatorStatusColor(
                getString(resuming ? R.string.resuming_virtual_stick : R.string.arming_virtual_stick),
                getColor(R.color.text_accent_amber));
        findViewById(R.id.survey_planner_abort_button).setVisibility(View.VISIBLE);
        appendLog("SURVEY " + (resuming ? "resume" : "arm") + " requested · environment="
                + surveyExecutionEnvironment + " · manual takeoff on real aircraft");
        aircraftBridge.enableVirtualStickForManual(() -> onSurveySimulatorVirtualStickReady(resuming));
    }

    private String importedMissionCameraIssue() {
        SurveyMission mission = surveyMission;
        if (mission == null || mission.getActiveMapping() == null) return null;
        boolean usesUeCamera = hilVirtualFramesEnabled && aircraftSnapshot.getSimulatorActive();
        if (usesUeCamera) return null;
        if (aircraftBridge == null) return getString(R.string.current_camera_disconnected);
        DjiCameraProfileCatalog.Resolution resolution =
                aircraftBridge.currentSurveyCameraResolution();
        ImportedMissionCameraCompatibility compatibility =
                ImportedMissionCameraCompatibilityPolicy.INSTANCE.evaluate(
                        mission,
                        resolution.getProfile(),
                        aircraftBridge.isSurveyCameraConnected(),
                        resolution.getVerifiedProfile(),
                        this);
        if (compatibility.getCompatible()) return null;
        return getString(R.string.imported_mission_camera_incompatible,
                String.join("\n", compatibility.getReasons()));
    }

    private String terrainTakeoffReferenceIssue() {
        SurveyMission mission = surveyMission;
        if (mission == null || mission.getTerrainPlan() == null) return null;
        GeoPoint home = Double.isFinite(aircraftSnapshot.getHomeLatitude())
                && Double.isFinite(aircraftSnapshot.getHomeLongitude())
                ? new GeoPoint(aircraftSnapshot.getHomeLatitude(),
                        aircraftSnapshot.getHomeLongitude(), 0.0)
                : null;
        SurveyTerrainTakeoffVerification verification =
                SurveyTerrainTakeoffReferencePolicy.INSTANCE.verify(
                        mission.getTerrainPlan(), home, surveyTerrain, this);
        return verification.getValid() ? null : verification.getReason();
    }

    private void startSurveyUiDryRun() {
        if (surveyMission == null || surveyMission.getWaypoints().isEmpty()) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_missing_valid_mission), true);
            return;
        }
        boolean resuming = surveyUiDryRunMode && surveySimulatorExecution != null
                && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.PAUSED;
        SurveyExecutionGateResult allowed = new SurveyExecutionGateResult(
                true, java.util.Collections.emptySet(), 0.0);
        if (!resuming) {
            stopSurveyReplay(true);
            GeoPoint dryRunPoint = currentSurveyTakeoffPoint();
            surveySimulatorExecution = new SurveySimulatorExecutionStateMachine(
                    surveyMission, dryRunPoint, dryRunPoint);
            surveySimulatorExecution.requestArm(allowed);
            surveySimulatorExecution.onVirtualStickReady(allowed);
            surveyUiDryRunMode = true;
            appendLog("SURVEY UI DRY RUN RUNNING · NO_CONTROL · NO_CAMERA");
        } else {
            surveySimulatorExecution.resume(allowed);
            appendLog("SURVEY UI DRY RUN RESUMED · NO_CONTROL · NO_CAMERA");
        }
        renderSurveySimulatorExecutionStatus(surveySimulatorExecution.getStatus(), null);
        renderSurveyStatus(getString(R.string.survey_ui_simulation_running));
        mainHandler.removeCallbacks(surveyUiDryRunRunnable);
        mainHandler.postDelayed(surveyUiDryRunRunnable, 450L);
    }

    private void startSurveyAutoTakeoff() {
        if (surveyAutoTakeoffPending) {
            showBanner(getString(R.string.survey_auto_takeoff_waiting_stable));
            return;
        }
        if (controlArmed || autoInferenceEnabled || chunkExecutionActive
                || activeRelativeMoveRunnable != null || aircraftSnapshot.getVirtualStickEnabled()) {
            setSurveySimulatorStatus(getString(R.string.survey_gate_other_control), true);
            return;
        }
        SurveyExecutionGateResult preflight = currentSurveySimulatorGate(false, true);
        if (!preflight.getAllowed()) {
            renderSurveySimulatorGateFailure(getString(R.string.auto_takeoff_blocked), preflight);
            appendLog("SURVEY auto takeoff blocked: " + preflight.getBlocks() + " · NO_CONTROL");
            return;
        }
        surveyAutoTakeoffPending = true;
        surveyAutoTakeoffDeadlineElapsedMs = SystemClock.elapsedRealtime() + 30_000L;
        surveyAutoTakeoffFlyingSinceElapsedMs = 0L;
        setSurveySimulatorStatus(getString(R.string.survey_auto_takeoff_waiting), false);
        appendLog("SURVEY auto takeoff requested · simulator-only · NO_VS");
        aircraftBridge.takeOff((ok, message) -> {
            if (!surveyAutoTakeoffPending) return;
            if (!ok) {
                cancelSurveyAutoTakeoff(getString(R.string.dji_auto_takeoff_failed, message));
                return;
            }
            mainHandler.removeCallbacks(surveyAutoTakeoffRunnable);
            mainHandler.post(surveyAutoTakeoffRunnable);
        });
    }

    private void cancelSurveyAutoTakeoff(String reason) {
        if (!surveyAutoTakeoffPending) return;
        surveyAutoTakeoffPending = false;
        surveyAutoTakeoffFlyingSinceElapsedMs = 0L;
        mainHandler.removeCallbacks(surveyAutoTakeoffRunnable);
        setSurveySimulatorStatus(getString(R.string.survey_auto_takeoff_cancelled, reason), true);
        appendLog("SURVEY auto takeoff cancelled: " + reason + " · NO_VS");
    }

    private void onSurveySimulatorVirtualStickReady(boolean resuming) {
        if (mockUiActive || surveySimulatorExecution == null) {
            abortSurveySimulatorExecution(getString(R.string.control_context_invalid), true);
            return;
        }
        SurveyExecutionGateResult runGate = resuming
                ? currentSurveySimulatorGate(true, false, false)
                : currentSurveySimulatorGate(true);
        SurveyExecutionStatus executionStatus = resuming
                ? surveySimulatorExecution.resume(runGate, currentAircraftGeoPoint(),
                        aircraftSnapshot.getHeading())
                : surveySimulatorExecution.onVirtualStickReady(runGate);
        if (executionStatus.getState() != SurveyExecutionState.RUNNING) {
            renderSurveySimulatorExecutionStatus(executionStatus, null);
            if (executionStatus.getState() == SurveyExecutionState.PAUSED) {
                persistSurveyCheckpoint(executionStatus);
                aircraftBridge.disableVirtualStick(getString(R.string.resume_recheck_failed_keep_paused));
                appendLog("SURVEY remained PAUSED: " + executionStatus.getReason()
                        + " · checkpoint retained");
                return;
            }
            abortSurveySimulatorExecution(executionStatus.getReason() == null
                    ? getString(R.string.safety_recheck_failed_after_vs_ready)
                    : executionStatus.getReason(), true);
            return;
        }
        if (resuming) {
            surveyGimbalCommandedPitch = Double.NaN;
            surveyGimbalCommandAcceptedElapsedMs = 0L;
            surveyGimbalVerificationStartedElapsedMs = 0L;
            surveyGimbalLastCommandElapsedMs = 0L;
            surveyGimbalCommandGeneration++;
        }
        setSurveyWaypointDeadline(executionStatus.getWaypointIndex());
        surveyControlStartedElapsedMs = SystemClock.elapsedRealtime();
        surveyNextControlTickUptimeMs = SystemClock.uptimeMillis();
        surveyControlTickCount = 0L;
        surveyFreshFlightStateCount = 0L;
        surveyLastFlightStateUpdatedAtMs = 0L;
        lastSurveyExecutionRenderElapsedMs = 0L;
        lastSurveyFlightLogElapsedMs = 0L;
        mainHandler.removeCallbacks(surveySimulatorControlRunnable);
        mainHandler.post(surveySimulatorControlRunnable);
        appendLog("SURVEY execution RUNNING waypoint=" + (executionStatus.getWaypointIndex() + 1)
                + "/" + surveyMission.getWaypoints().size());
        publishSurveyTargetToUe();
        persistSurveyCheckpoint(executionStatus);
        renderSurveySimulatorExecutionStatus(executionStatus, null);
    }

    private void runSurveySimulatorControlTick() {
        if (surveySimulatorExecution == null || surveyMission == null) return;
        SurveyExecutionStatus executionStatus = surveySimulatorExecution.getStatus();
        if (executionStatus.getState() != SurveyExecutionState.RUNNING) return;
        surveyControlTickCount++;
        if (aircraftSnapshot.getFlightStateUpdatedAtMs() != surveyLastFlightStateUpdatedAtMs) {
            surveyLastFlightStateUpdatedAtMs = aircraftSnapshot.getFlightStateUpdatedAtMs();
            surveyFreshFlightStateCount++;
        }
        long nowElapsedMs = SystemClock.elapsedRealtime();
        if (nowElapsedMs - lastSurveyFlightLogElapsedMs >= 1_000L) {
            lastSurveyFlightLogElapsedMs = nowElapsedMs;
            appendSurveyFlightSample(executionStatus);
        }
        SurveyExecutionGateResult gate = currentSurveySimulatorGate(true, false, false);
        SurveyFailsafeDecision failsafe = SurveyExecutionWatchdog.INSTANCE.inspect(
                executionStatus.getState(), gate, !mockUiActive,
                nowElapsedMs, surveyWaypointDeadlineElapsedMs);
        if (failsafe.getAction() == SurveyFailsafeAction.PAUSE_ZERO_AND_RELEASE) {
            String reason = failsafe.getReason() == null
                    ? getString(R.string.runtime_safety_temporarily_lost) : failsafe.getReason();
            if (SurveyRuntimeFaultPolicy.isTimeout(reason)
                    || reason.startsWith("runtime gate:")) {
                pauseSurveyForRecoverableFault(reason, true);
            } else {
                pauseSurveyForExternalIntervention(reason, true);
            }
            return;
        }
        if (failsafe.getAction() == SurveyFailsafeAction.ABORT_ZERO_AND_RELEASE) {
            abortSurveySimulatorExecution(failsafe.getReason() == null
                    ? getString(R.string.runtime_safety_gate_failed) : failsafe.getReason(), true);
            return;
        }
        int waypointIndex = executionStatus.getWaypointIndex();
        if (waypointIndex < 0 || waypointIndex >= surveyMission.getWaypoints().size()) {
            abortSurveySimulatorExecution(getString(R.string.waypoint_index_out_of_range), true);
            return;
        }

        SurveyWaypoint target = surveySimulatorExecution.getCurrentTarget();
        int executionLegIndex = surveySimulatorExecution.getExecutionLegIndex();
        double actualGimbalPitch = aircraftSnapshot.getGimbalPitch();
        if (!Double.isFinite(surveyGimbalCommandedPitch)
                || Math.abs(surveyGimbalCommandedPitch - target.getGimbalPitchDegrees()) > 0.1) {
            surveyGimbalCommandedPitch = target.getGimbalPitchDegrees();
            surveyGimbalSettlingStartedElapsedMs = 0L;
            surveyGimbalLastCommandElapsedMs = 0L;
            surveyGimbalCommandAcceptedElapsedMs = 0L;
            surveyGimbalVerificationStartedElapsedMs = nowElapsedMs;
            surveyGimbalCommandGeneration++;
            surveyGimbalCommandAttempts = 0;
        }
        boolean gimbalSettled = SurveyGimbalSettlePolicy.INSTANCE.isSettled(
                target.getGimbalPitchDegrees(), actualGimbalPitch);
        boolean gimbalVerifiedForCapture = SurveyGimbalSettlePolicy.INSTANCE.isVerifiedForCapture(
                target.getGimbalPitchDegrees(), actualGimbalPitch,
                surveyGimbalCommandAcceptedElapsedMs, nowElapsedMs);
        boolean nadirPitchLimited = SurveyNadirGimbalPolicy.INSTANCE.isMechanicalLimit(
                target.getGimbalPitchDegrees(), gimbalSettled,
                aircraftSnapshot.getGimbalPitchAtStop());
        if (nadirPitchLimited != surveyGimbalLimitActive) {
            surveyGimbalLimitActive = nadirPitchLimited;
            appendLog(String.format(Locale.US,
                    "SURVEY nadir gimbal limit %s target=%.0f° actual=%.1f° aircraftPitch=%+.1f°",
                    nadirPitchLimited ? "ACTIVE" : "CLEARED",
                    target.getGimbalPitchDegrees(), actualGimbalPitch,
                    aircraftSnapshot.getAircraftPitch()));
        }
        surveyGimbalSettlingStartedElapsedMs = SurveyGimbalSettlePolicy.INSTANCE.updateUnsettledSince(
                nowElapsedMs, surveyGimbalSettlingStartedElapsedMs,
                gimbalSettled || nadirPitchLimited);
        if (!gimbalVerifiedForCapture && !nadirPitchLimited
                && SurveyGimbalSettlePolicy.INSTANCE.hasTimedOut(
                nowElapsedMs, surveyGimbalVerificationStartedElapsedMs)) {
            pauseSurveyForRecoverableFault(String.format(Locale.US,
                    "gimbal pitch timeout target=%.0f° actual=%.1f° attempts=%d",
                    target.getGimbalPitchDegrees(), actualGimbalPitch,
                    surveyGimbalCommandAttempts), true);
            return;
        }
        if (!gimbalVerifiedForCapture && !nadirPitchLimited && SurveyGimbalSettlePolicy.INSTANCE.shouldRetry(
                nowElapsedMs, surveyGimbalLastCommandElapsedMs)) {
            surveyGimbalLastCommandElapsedMs = nowElapsedMs;
            surveyGimbalCommandAttempts++;
            issueSurveyGimbalCommand(target.getGimbalPitchDegrees());
            appendLog(String.format(Locale.US,
                    "SURVEY gimbal command target=%.0f° actual=%.1f° aircraftPitch=%+.1f° "
                            + "pitchAtStop=%s motorOverloaded=%s mode=%s attempt=%d",
                    target.getGimbalPitchDegrees(), actualGimbalPitch,
                    aircraftSnapshot.getAircraftPitch(),
                    aircraftSnapshot.getGimbalPitchAtStop(),
                    aircraftSnapshot.getGimbalMotorOverloaded(),
                    aircraftSnapshot.getGimbalMode(),
                    surveyGimbalCommandAttempts));
        }
        double maximumSpeed = surveyHorizontalSpeedLimit();
        double maximumVerticalSpeed = surveyVerticalSpeedLimit();
        SurveyFollowerCommand command = SurveyWaypointFollower.INSTANCE.command(
                new SurveyFollowerPose(
                        aircraftSnapshot.getLatitude(), aircraftSnapshot.getLongitude(),
                        aircraftSnapshot.getAltitude(), aircraftSnapshot.getHeading()),
                target, maximumSpeed, maximumVerticalSpeed);
        if (command.getHorizontalErrorMeters() + 0.5
                < surveyBestWaypointHorizontalErrorMeters) {
            surveyBestWaypointHorizontalErrorMeters = command.getHorizontalErrorMeters();
            surveyLastWaypointProgressElapsedMs = nowElapsedMs;
        } else if (SurveyWaypointDivergencePolicy.INSTANCE.shouldPause(
                surveyBestWaypointHorizontalErrorMeters,
                command.getHorizontalErrorMeters(),
                surveyLastWaypointProgressElapsedMs,
                nowElapsedMs)) {
            pauseSurveyForRecoverableFault(getString(
                    R.string.survey_waypoint_diverging,
                    surveyBestWaypointHorizontalErrorMeters,
                    command.getHorizontalErrorMeters()), true);
            return;
        }
        GeoPoint currentPoint = new GeoPoint(
                aircraftSnapshot.getLatitude(), aircraftSnapshot.getLongitude(),
                aircraftSnapshot.getAltitude());
        if (surveyPendingCaptureStart != null && gimbalVerifiedForCapture && !surveyPhotoInFlight) {
            SurveyWaypoint pendingStart = surveyPendingCaptureStart;
            surveyPendingCaptureStart = null;
            boolean photoTriggered = surveyCaptureController.onWaypointReached(
                    pendingStart, currentPoint, nowElapsedMs, true);
            appendLog(String.format(Locale.US,
                    "SURVEY delayed nadir capture START actual=%.1f° aircraftPitch=%+.1f°",
                    actualGimbalPitch, aircraftSnapshot.getAircraftPitch()));
            if (photoTriggered) triggerSurveyPhoto("DELAYED_START_DISTANCE_INTERVAL");
        }
        if (!command.getReached() && SurveyNadirGimbalPolicy.INSTANCE.canCapture(gimbalVerifiedForCapture)
                && surveySimulatorExecution.getCurrentPhase() == SurveyExecutionPhase.SURVEY
                && surveyCaptureController.onPosition(
                currentPoint, SystemClock.elapsedRealtime(), !surveyPhotoInFlight,
                aircraftSnapshot.getHorizontalSpeed())) {
            triggerSurveyPhoto(surveyMission.getConstraints().getCaptureTriggerMode()
                    == SurveyCaptureTriggerMode.TIME ? "time interval" : "distance interval");
        }
        if (command.getReached()) {
            boolean startsCapture = target.getCaptureAction()
                    == edu.playground.djivln.survey.CaptureAction.START_DISTANCE_INTERVAL;
            boolean stopsCapture = target.getCaptureAction()
                    == edu.playground.djivln.survey.CaptureAction.STOP_DISTANCE_INTERVAL;
            boolean deferNadirCaptureStart = SurveyNadirGimbalPolicy.INSTANCE
                    .shouldDeferCaptureStart(target.getCaptureAction(), nadirPitchLimited);
            if (!deferNadirCaptureStart) {
                aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
            }
            if (!gimbalVerifiedForCapture && !deferNadirCaptureStart && !stopsCapture) {
                renderSurveySimulatorExecutionStatus(executionStatus, command);
                scheduleNextSurveyControlTick();
                return;
            }
            if (stopsCapture && surveyPendingCaptureStart != null) {
                aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
                surveyPendingCaptureStart = null;
                surveyCaptureController.reset();
                pauseSurveyForRecoverableFault(
                        getString(R.string.nadir_pass_gimbal_limit_no_capture),
                        true);
                return;
            }
            if (deferNadirCaptureStart) {
                surveyPendingCaptureStart = target;
                appendLog(String.format(Locale.US,
                        "SURVEY nadir capture deferred at pass start actual=%.1f° aircraftPitch=%+.1f°; "
                                + "continue flight until gimbal clears limit",
                        actualGimbalPitch, aircraftSnapshot.getAircraftPitch()));
            } else if (SurveyNadirGimbalPolicy.INSTANCE.shouldSkipEndFrame(
                    target.getCaptureAction(), gimbalVerifiedForCapture)) {
                surveyCaptureController.reset();
                appendLog(String.format(Locale.US,
                        "SURVEY nadir end frame skipped at mechanical limit actual=%.1f° aircraftPitch=%+.1f°",
                        actualGimbalPitch, aircraftSnapshot.getAircraftPitch()));
            } else if (target.getCaptureAction()
                    != edu.playground.djivln.survey.CaptureAction.CAPTURE_ON_REACH
                    || surveyPointCaptureCompletedLegIndex != executionLegIndex) {
                boolean photoTriggered = surveyCaptureController.onWaypointReached(
                        target, currentPoint, SystemClock.elapsedRealtime(), !surveyPhotoInFlight);
                if (photoTriggered) {
                    if (target.getCaptureAction()
                            == edu.playground.djivln.survey.CaptureAction.CAPTURE_ON_REACH) {
                        surveyPointCapturePendingLegIndex = executionLegIndex;
                    }
                    triggerSurveyPhoto(target.getCaptureAction().name());
                }
            }
            if (target.getCaptureAction()
                    == edu.playground.djivln.survey.CaptureAction.CAPTURE_ON_REACH
                    && surveyPointCaptureCompletedLegIndex != executionLegIndex) {
                renderSurveySimulatorExecutionStatus(executionStatus, command);
                scheduleNextSurveyControlTick();
                return;
            }
            if (target.getCaptureAction()
                    == edu.playground.djivln.survey.CaptureAction.STOP_DISTANCE_INTERVAL
                    && (surveyCaptureController.getActive() || surveyPhotoInFlight)) {
                renderSurveySimulatorExecutionStatus(executionStatus, command);
                scheduleNextSurveyControlTick();
                return;
            }
            SurveyExecutionPhase reachedPhase = surveySimulatorExecution.getCurrentPhase();
            long reachedAtElapsedMs = SystemClock.elapsedRealtime();
            executionStatus = surveySimulatorExecution.reachWaypoint();
            publishSurveyTargetToUe();
            renderSurveyExecutionOverlay(true);
            persistSurveyCheckpoint(executionStatus);
            appendLog(String.format(Locale.US,
                    "SURVEY leg reached %d/%d phase=%s elapsed=%.1fs h_err=%.2fm v_err=%+.2fm",
                    executionLegIndex + 1, surveySimulatorExecution.getExecutionLegCount(),
                    reachedPhase, Math.max(0L, reachedAtElapsedMs - surveyLegStartedElapsedMs) / 1000.0,
                    command.getHorizontalErrorMeters(), command.getVerticalErrorMeters()));
            if (executionStatus.getState() == SurveyExecutionState.COMPLETED) {
                completeSurveySimulatorExecution();
                return;
            }
            setSurveyWaypointDeadline(executionStatus.getWaypointIndex());
            renderSurveySimulatorExecutionStatus(executionStatus, null);
            scheduleNextSurveyControlTick();
            return;
        }

        aircraftBridge.sendBodyVelocity(
                (float) command.getForwardMetersPerSecond(),
                (float) command.getRightMetersPerSecond(),
                (float) command.getUpMetersPerSecond(),
                (float) command.getYawRateDegreesPerSecond());
        long now = SystemClock.elapsedRealtime();
        if (now - lastSurveyExecutionRenderElapsedMs >= 500L) {
            lastSurveyExecutionRenderElapsedMs = now;
            renderSurveySimulatorExecutionStatus(executionStatus, command);
        }
        scheduleNextSurveyControlTick();
    }

    private void issueSurveyGimbalCommand(double targetPitchDegrees) {
        long generation = ++surveyGimbalCommandGeneration;
        surveyGimbalCommandAcceptedElapsedMs = 0L;
        aircraftBridge.setSurveyGimbalPitch(targetPitchDegrees, (ok, message) -> {
            if (generation != surveyGimbalCommandGeneration) return;
            if (ok) {
                surveyGimbalCommandAcceptedElapsedMs = SystemClock.elapsedRealtime();
                appendLog(String.format(Locale.US,
                        "SURVEY gimbal command accepted target=%.0f° settle>=%.1fs",
                        targetPitchDegrees,
                        SurveyGimbalSettlePolicy.MIN_SETTLE_AFTER_ACCEPT_MS / 1000.0));
            } else {
                surveyGimbalCommandAcceptedElapsedMs = 0L;
            }
        });
    }

    private void scheduleNextSurveyControlTick() {
        surveyNextControlTickUptimeMs += SURVEY_CONTROL_INTERVAL_MS;
        long nowUptimeMs = SystemClock.uptimeMillis();
        if (surveyNextControlTickUptimeMs <= nowUptimeMs) {
            // Do not burst stale VS frames after a busy main-thread interval.
            surveyNextControlTickUptimeMs = nowUptimeMs + SURVEY_CONTROL_INTERVAL_MS;
        }
        mainHandler.postAtTime(surveySimulatorControlRunnable, surveyNextControlTickUptimeMs);
    }

    private void setSurveyWaypointDeadline(int waypointIndex) {
        surveyLegStartedElapsedMs = SystemClock.elapsedRealtime();
        surveyBestWaypointHorizontalErrorMeters = Double.POSITIVE_INFINITY;
        surveyLastWaypointProgressElapsedMs = surveyLegStartedElapsedMs;
        SurveyWaypoint target = surveySimulatorExecution.getCurrentTarget();
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        SurveyFollowerCommand estimate = SurveyWaypointFollower.INSTANCE.command(
                new SurveyFollowerPose(snapshot.getLatitude(), snapshot.getLongitude(),
                        snapshot.getAltitude(), snapshot.getHeading()),
                target, surveyHorizontalSpeedLimit(), surveyVerticalSpeedLimit());
        double horizontalSeconds = estimate.getHorizontalErrorMeters() / Math.max(0.1,
                surveyHorizontalSpeedLimit());
        double verticalSeconds = Math.abs(estimate.getVerticalErrorMeters()) /
                surveyVerticalSpeedLimit();
        long allowanceMs = (long) (Math.max(30.0,
                horizontalSeconds * 4.0 + verticalSeconds * 2.0 + 15.0) * 1_000.0);
        surveyWaypointDeadlineElapsedMs = surveyLegStartedElapsedMs + allowanceMs;
    }

    private double surveyHorizontalSpeedLimit() {
        SurveyCaptureView captureView = surveySimulatorExecution == null
                ? SurveyCaptureView.NADIR
                : surveySimulatorExecution.getCurrentTarget().getCaptureView();
        return Math.max(0.1, Math.min(10.0,
                surveyMission.getConstraints().speedForCaptureView(captureView)));
    }

    private double surveyVerticalSpeedLimit() {
        if (surveySimulatorExecution != null
                && surveySimulatorExecution.getCurrentPhase() == SurveyExecutionPhase.SAFE_CLIMB) {
            return Math.max(0.1, Math.min(10.0,
                    surveyMission.getConstraints().getTakeoffSpeedMetersPerSecond()));
        }
        if (surveySimulatorExecution != null) {
            GeoPoint current = currentAircraftGeoPoint();
            SurveyWaypoint target = surveySimulatorExecution.getCurrentTarget();
            if (current != null && target != null
                    && target.getPoint().getAltitudeMeters() < current.getAltitudeMeters()) {
                return Math.max(0.1, Math.min(
                        SurveyWaypointFollower.MAX_VERTICAL_SPEED_METERS_PER_SECOND,
                        surveyMission.getConstraints().getDescentSpeedMetersPerSecond()));
            }
        }
        return SurveyWaypointFollower.MAX_VERTICAL_SPEED_METERS_PER_SECOND;
    }

    private void pauseSurveySimulatorExecution() {
        if (surveySimulatorExecution == null
                || surveySimulatorExecution.getStatus().getState() != SurveyExecutionState.RUNNING) {
            setSurveySimulatorStatus(getString(R.string.survey_no_pause_needed), true);
            return;
        }
        mainHandler.removeCallbacks(surveySimulatorControlRunnable);
        mainHandler.removeCallbacks(surveyUiDryRunRunnable);
        invalidateSurveyPhotoRequest();
        SurveyExecutionStatus executionStatus = surveySimulatorExecution.pause(
                null, currentAircraftGeoPoint());
        persistSurveyCheckpoint(executionStatus);
        if (surveyUiDryRunMode) {
            appendLog("SURVEY UI DRY RUN PAUSED · NO_CONTROL");
        } else if (aircraftBridge != null) {
            aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
            aircraftBridge.disableVirtualStick(getString(R.string.survey_mission_paused));
        }
        appendLog("SURVEY execution PAUSED waypoint=" + (executionStatus.getWaypointIndex() + 1));
        renderSurveySimulatorExecutionStatus(executionStatus, null);
    }

    private void monitorSurveyExternalIntervention(Mini2AircraftBridge.Snapshot snapshot) {
        if (!isSurveySimulatorExecutionActive()) return;
        if (snapshot.getGoingHome()) {
            pauseSurveyForExternalIntervention(getString(R.string.flight_controller_entered_rth), true);
        } else if (snapshot.getLanding()) {
            pauseSurveyForExternalIntervention(getString(R.string.flight_controller_entered_landing), true);
        }
    }

    private boolean pauseSurveyForExternalIntervention(String reason, boolean releaseVirtualStick) {
        return pauseSurveyExecution(reason, releaseVirtualStick,
                "external intervention", getString(R.string.external_intervention_route_paused_saved));
    }

    private boolean pauseSurveyForRecoverableFault(String reason, boolean releaseVirtualStick) {
        return pauseSurveyExecution(reason, releaseVirtualStick,
                "recoverable fault", getString(R.string.runtime_fault_route_paused_saved));
    }

    private boolean pauseSurveyExecution(String reason, boolean releaseVirtualStick,
                                         String logCategory, String banner) {
        if (surveySimulatorExecution == null) return false;
        SurveyExecutionState state = surveySimulatorExecution.getStatus().getState();
        if (state != SurveyExecutionState.RUNNING && state != SurveyExecutionState.ARMING) {
            return state == SurveyExecutionState.PAUSED;
        }
        mainHandler.removeCallbacks(surveySimulatorControlRunnable);
        mainHandler.removeCallbacks(surveyUiDryRunRunnable);
        invalidateSurveyPhotoRequest();
        SurveyExecutionStatus paused = surveySimulatorExecution.pause(
                reason, currentAircraftGeoPoint());
        persistSurveyCheckpoint(paused);
        if (!surveyUiDryRunMode && aircraftBridge != null) {
            aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
            if (releaseVirtualStick) aircraftBridge.disableVirtualStick(reason);
        }
        appendLog("SURVEY " + logCategory + " PAUSED: " + reason
                + " · checkpoint retained · zero velocity"
                + (releaseVirtualStick ? " · VS released" : ""));
        renderSurveySimulatorExecutionStatus(paused, null);
        showBanner(banner);
        return true;
    }

    private void pauseSurveyForLifecycle(String reason) {
        if (surveySimulatorExecution == null) return;
        SurveyExecutionState state = surveySimulatorExecution.getStatus().getState();
        if (state == SurveyExecutionState.RUNNING) {
            mainHandler.removeCallbacks(surveySimulatorControlRunnable);
            mainHandler.removeCallbacks(surveyUiDryRunRunnable);
            invalidateSurveyPhotoRequest();
            SurveyExecutionStatus paused = surveySimulatorExecution.pause(
                    null, currentAircraftGeoPoint());
            persistSurveyCheckpoint(paused);
            if (surveyUiDryRunMode) {
                appendLog("SURVEY UI DRY RUN PAUSED · lifecycle=" + reason + " · NO_CONTROL");
            } else if (aircraftBridge != null) {
                aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
                aircraftBridge.disableVirtualStick(reason);
            }
            appendLog("SURVEY lifecycle PAUSED: " + reason + " · checkpoint retained");
            renderSurveySimulatorExecutionStatus(paused, null);
        } else if (state == SurveyExecutionState.ARMING) {
            invalidateSurveyPhotoRequest();
            SurveyExecutionStatus paused = surveySimulatorExecution.pause(
                    null, currentAircraftGeoPoint());
            persistSurveyCheckpoint(paused);
            if (aircraftBridge != null) aircraftBridge.disableVirtualStick(reason);
        }
    }

    private boolean shouldForceSurveyLowBatteryReturn() {
        return SurveyLowBatteryPolicy.INSTANCE.shouldTrigger(
                aircraftSnapshot.getAircraftBattery(), aircraftSnapshot.getFlying(),
                aircraftSnapshot.getSimulatorActive(), surveySimulatorExecution == null
                        ? null : surveySimulatorExecution.getStatus().getState());
    }

    private void monitorSurveyLowBatteryReturn() {
        if (!shouldForceSurveyLowBatteryReturn()) {
            if (!aircraftSnapshot.getFlying()) {
                surveyLowBatteryReturnIssued = false;
                cancelSurveyLowBatteryReturn(getString(R.string.aircraft_landed));
            }
            return;
        }
        if (surveyLowBatteryReturnIssued || surveyLowBatteryReturnDeadlineElapsedMs != 0L) return;
        pauseSurveySimulatorExecution();
        surveyLowBatteryReturnDeadlineElapsedMs = SystemClock.elapsedRealtime()
                + SurveyLowBatteryPolicy.AUTO_RETURN_COUNTDOWN_MILLIS;
        appendLog("SURVEY LOW BATTERY " + aircraftSnapshot.getAircraftBattery()
                + "% · auto RTH countdown=5s · checkpoint retained");
        updateSurveyLowBatteryDialog(5);
        mainHandler.removeCallbacks(surveyLowBatteryReturnRunnable);
        mainHandler.post(surveyLowBatteryReturnRunnable);
    }

    private void updateSurveyLowBatteryDialog(int secondsRemaining) {
        String message = getString(R.string.survey_low_battery_rth_message,
                aircraftSnapshot.getAircraftBattery(), secondsRemaining);
        if (surveyLowBatteryDialog == null || !surveyLowBatteryDialog.isShowing()) {
            surveyLowBatteryDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.survey_low_battery_rth_title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.survey_return_home_now,
                            (dialog, which) -> issueSurveyLowBatteryReturn())
                    .create();
            surveyLowBatteryDialog.setCanceledOnTouchOutside(false);
            surveyLowBatteryDialog.show();
        } else {
            surveyLowBatteryDialog.setMessage(message);
        }
    }

    private void issueSurveyLowBatteryReturn() {
        if (surveyLowBatteryReturnIssued) return;
        surveyLowBatteryReturnIssued = true;
        surveyLowBatteryReturnDeadlineElapsedMs = 0L;
        mainHandler.removeCallbacks(surveyLowBatteryReturnRunnable);
        if (surveyLowBatteryDialog != null) {
            surveyLowBatteryDialog.dismiss();
            surveyLowBatteryDialog = null;
        }
        if (aircraftBridge != null) {
            aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
            surveyLowBatteryGoHomeCommandSent = false;
            aircraftBridge.disableVirtualStickThen(getString(R.string.low_battery_auto_rth),
                    this::sendSurveyLowBatteryGoHomeCommand);
            // A delayed/failed VS callback must not suppress the safety RTH.
            mainHandler.postDelayed(this::sendSurveyLowBatteryGoHomeCommand, 1_500L);
        }
        showBanner(getString(R.string.survey_low_battery_rth_started));
    }

    private void sendSurveyLowBatteryGoHomeCommand() {
        if (surveyLowBatteryGoHomeCommandSent || aircraftBridge == null) return;
        surveyLowBatteryGoHomeCommandSent = true;
        appendLog("SURVEY LOW BATTERY · DJI RTH requested");
        aircraftBridge.startGoHome();
    }

    private void cancelSurveyLowBatteryReturn(String reason) {
        if (surveyLowBatteryReturnDeadlineElapsedMs == 0L && surveyLowBatteryDialog == null) return;
        surveyLowBatteryReturnDeadlineElapsedMs = 0L;
        mainHandler.removeCallbacks(surveyLowBatteryReturnRunnable);
        if (surveyLowBatteryDialog != null) {
            surveyLowBatteryDialog.dismiss();
            surveyLowBatteryDialog = null;
        }
        appendLog("SURVEY low battery countdown cleared: " + reason);
    }

    private void completeSurveySimulatorExecution() {
        mainHandler.removeCallbacks(surveySimulatorControlRunnable);
        mainHandler.removeCallbacks(surveyUiDryRunRunnable);
        surveyCaptureController.reset();
        surveyPendingCaptureStart = null;
        surveyPointCapturePendingLegIndex = -1;
        surveyPointCaptureCompletedLegIndex = -1;
        surveyGimbalLimitActive = false;
        invalidateSurveyPhotoRequest();
        if (!surveyUiDryRunMode && aircraftBridge != null) {
            aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
            aircraftBridge.disableVirtualStick(getString(R.string.simulated_route_completion_action_complete));
        }
        SurveyExecutionStatus executionStatus = surveySimulatorExecution == null
                ? null : surveySimulatorExecution.getStatus();
        appendLog("SURVEY execution COMPLETED · zero velocity · VS released · action="
                + (surveyMission == null ? "UNKNOWN"
                : surveyMission.getConstraints().getCompletionAction()));
        appendSurveyControlRates("COMPLETED");
        clearPersistedSurveyCheckpoint();
        if (executionStatus != null) renderSurveySimulatorExecutionStatus(executionStatus, null);
        surveyUiDryRunMode = false;
    }

    private void appendSurveyControlRates(String outcome) {
        long elapsedMs = Math.max(1L,
                SystemClock.elapsedRealtime() - surveyControlStartedElapsedMs);
        appendLog(String.format(Locale.US,
                "SURVEY rates %s · VS loop %.1fHz · fresh FC state %.1fHz · ticks=%d states=%d",
                outcome, surveyControlTickCount * 1000.0 / elapsedMs,
                surveyFreshFlightStateCount * 1000.0 / elapsedMs,
                surveyControlTickCount, surveyFreshFlightStateCount));
    }

    private void abortSurveySimulatorExecution(String reason, boolean releaseVirtualStick) {
        cancelSurveyAutoTakeoff(reason);
        if (surveySimulatorExecution == null) return;
        SurveyExecutionState state = surveySimulatorExecution.getStatus().getState();
        if (state == SurveyExecutionState.IDLE || state == SurveyExecutionState.COMPLETED) return;
        mainHandler.removeCallbacks(surveySimulatorControlRunnable);
        mainHandler.removeCallbacks(surveyUiDryRunRunnable);
        surveyCaptureController.reset();
        surveyPendingCaptureStart = null;
        surveyPointCapturePendingLegIndex = -1;
        surveyPointCaptureCompletedLegIndex = -1;
        surveyGimbalLimitActive = false;
        invalidateSurveyPhotoRequest();
        SurveyExecutionStatus executionStatus = state == SurveyExecutionState.ABORTED
                ? surveySimulatorExecution.getStatus() : surveySimulatorExecution.abort(reason);
        boolean uiDryRun = surveyUiDryRunMode;
        if (!uiDryRun && aircraftBridge != null) {
            aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
            if (releaseVirtualStick) aircraftBridge.disableVirtualStick(reason);
        }
        appendLog((uiDryRun ? "SURVEY UI DRY RUN ABORTED: " : "SURVEY execution ABORTED: ")
                + reason + (uiDryRun ? " · NO_CONTROL" : " · zero velocity"
                + (releaseVirtualStick ? " · VS released" : "")));
        clearPersistedSurveyCheckpoint();
        renderSurveySimulatorExecutionStatus(executionStatus, null);
        surveyUiDryRunMode = false;
    }

    private void triggerSurveyPhoto(String reason) {
        if (surveyPhotoInFlight || aircraftBridge == null) return;
        surveyPhotoInFlight = true;
        long requestGeneration = ++surveyPhotoRequestGeneration;
        boolean useUeFrame = hilVirtualFramesEnabled && aircraftSnapshot.getSimulatorActive();
        appendLog(String.format(Locale.US,
                "SURVEY photo requested: %s source=%s latency=%dms lead=%.2fm speed=%.2fm/s",
                reason, useUeFrame ? "UE" : "DJI",
                surveyCaptureController.getEstimatedCaptureLatencyMillis(),
                surveyCaptureController.compensationLeadMeters(aircraftSnapshot.getHorizontalSpeed()),
                aircraftSnapshot.getHorizontalSpeed()));
        mainHandler.removeCallbacks(surveyPhotoTimeoutRunnable);
        mainHandler.postDelayed(surveyPhotoTimeoutRunnable, 8_000L);
        if (useUeFrame) {
            captureSurveyUeFrame(requestGeneration, reason);
            return;
        }
        final long[] triggerFrameId = {0L};
        aircraftBridge.takePhoto((triggeredAtNanos, triggeredAtEpochMillis) -> {
            if (requestGeneration != surveyPhotoRequestGeneration || !surveyPhotoInFlight) return;
            triggerFrameId[0] = beginTriggerAlignedFrameCapture(
                    reason, triggeredAtNanos, triggeredAtEpochMillis);
            surveyTriggerFrameId = triggerFrameId[0];
        }, (ok, message) -> {
            runOnUiThread(() -> completeTriggerAlignedFrameCapture(
                    triggerFrameId[0], ok, message));
            finishSurveyPhotoRequest(requestGeneration, ok, message);
        });
    }

    private long beginTriggerAlignedFrameCapture(
            String reason, long triggeredAtElapsedNanos, long triggeredAtEpochMillis) {
        DJICodecManager codec = codecManager;
        if (codec == null) {
            appendLog("TRIGGER_FRAME skipped: DJI decoder unavailable · " + reason);
            return 0L;
        }
        if (aircraftBridge == null || !aircraftBridge.isTriggerAlignedFrameSourceUnambiguous()) {
            appendLog("TRIGGER_FRAME skipped: primary feed cannot be proven to match survey camera · "
                    + reason);
            return 0L;
        }
        long id = ++triggerFrameSequence;
        long baselineVideoSequence = decodedFrameSequence.get();
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        SurveyMission mission = surveyMission;
        SurveySimulatorExecutionStateMachine execution = surveySimulatorExecution;
        SurveyWaypoint captureTarget = execution == null ? null : execution.getCurrentTarget();
        TriggerFrameMetadata metadata = new TriggerFrameMetadata(
                triggeredAtEpochMillis,
                snapshot.getFlightStateUpdatedAtMs(),
                reason,
                captureTarget == null ? SurveyCaptureView.NADIR.name()
                        : captureTarget.getCaptureView().name(),
                snapshot.getProduct(),
                snapshot.getLatitude(), snapshot.getLongitude(), snapshot.getAltitude(),
                snapshot.getAsl(), snapshot.getGroundClearance(), snapshot.getHeading(),
                snapshot.getAircraftPitch(), snapshot.getGimbalPitch(),
                snapshot.getVelocityNorth(), snapshot.getVelocityEast(),
                -snapshot.getVerticalSpeed(),
                mission == null ? null : mission.getId(),
                execution == null ? null : execution.getExecutionLegIndex(),
                execution == null ? null : execution.getStatus().getWaypointIndex());
        PendingTriggerFrame pending = new PendingTriggerFrame(
                id, metadata, baselineVideoSequence, triggeredAtElapsedNanos);
        pendingTriggerFrames.put(id, pending);
        awaitFreshPostTriggerFrame(id);
        mainHandler.postDelayed(() -> expirePendingTriggerFrame(id), 3_000L);
        return id;
    }

    private void awaitFreshPostTriggerFrame(long id) {
        PendingTriggerFrame pending = pendingTriggerFrames.get(id);
        if (pending == null || pending.bitmapRequested) return;
        long nowNanos = SystemClock.elapsedRealtimeNanos();
        long candidateSequence = decodedFrameSequence.get();
        long receivedAtNanos = lastDecodedFrameRenderedElapsedNanos.get();
        if (!TriggerFrameFreshnessPolicy.INSTANCE.isFreshPostTriggerFrame(
                pending.baselineVideoSequence, candidateSequence,
                pending.triggeredAtElapsedNanos, receivedAtNanos, nowNanos)) {
            if (nowNanos - pending.triggeredAtElapsedNanos < 3_000_000_000L) {
                mainHandler.postDelayed(() -> awaitFreshPostTriggerFrame(id), 20L);
            }
            return;
        }
        DJICodecManager codec = codecManager;
        if (codec == null) return;
        Mini2AircraftBridge.Snapshot frameSnapshot = aircraftSnapshot;
        long frameEpochMillis = pending.metadata.getCapturedAtEpochMillis() + Math.max(0L,
                (receivedAtNanos - pending.triggeredAtElapsedNanos) / 1_000_000L);
        pending.metadata = new TriggerFrameMetadata(
                frameEpochMillis,
                frameSnapshot.getFlightStateUpdatedAtMs(),
                pending.metadata.getTriggerReason(),
                pending.metadata.getCaptureView(),
                frameSnapshot.getProduct(),
                frameSnapshot.getLatitude(), frameSnapshot.getLongitude(), frameSnapshot.getAltitude(),
                frameSnapshot.getAsl(), frameSnapshot.getGroundClearance(), frameSnapshot.getHeading(),
                frameSnapshot.getAircraftPitch(), frameSnapshot.getGimbalPitch(),
                frameSnapshot.getVelocityNorth(), frameSnapshot.getVelocityEast(),
                -frameSnapshot.getVerticalSpeed(),
                pending.metadata.getMissionId(), pending.metadata.getExecutionLegIndex(),
                pending.metadata.getWaypointIndex());
        pending.bitmapRequested = true;
        codec.getBitmap(bitmap -> runOnUiThread(() -> {
            PendingTriggerFrame current = pendingTriggerFrames.get(id);
            if (current == null) {
                if (bitmap != null) bitmap.recycle();
                return;
            }
            current.bitmap = bitmap;
            finalizeTriggerAlignedFrameIfReady(current);
        }));
    }

    private void completeTriggerAlignedFrameCapture(long id, boolean success, String message) {
        if (id <= 0L) return;
        PendingTriggerFrame pending = pendingTriggerFrames.get(id);
        if (pending == null) return;
        pending.photoSucceeded = success;
        pending.resultMessage = message;
        finalizeTriggerAlignedFrameIfReady(pending);
    }

    private void finalizeTriggerAlignedFrameIfReady(PendingTriggerFrame pending) {
        if (pending.photoSucceeded == null) return;
        if (!pending.photoSucceeded) {
            pendingTriggerFrames.remove(pending.id);
            if (pending.bitmap != null) pending.bitmap.recycle();
            return;
        }
        if (pending.bitmap == null) return;
        pendingTriggerFrames.remove(pending.id);
        Bitmap bitmap = pending.bitmap;
        storageExecutor.execute(() -> saveTriggerAlignedFrame(
                bitmap, pending.metadata, pending.resultMessage));
    }

    private void expirePendingTriggerFrame(long id) {
        PendingTriggerFrame pending = pendingTriggerFrames.remove(id);
        if (pending == null) return;
        if (pending.bitmap != null) pending.bitmap.recycle();
        appendLog(getString(R.string.trigger_frame_capture_timeout, pending.metadata.getTriggerReason()));
    }

    private void saveTriggerAlignedFrame(
            Bitmap bitmap, TriggerFrameMetadata metadata, String resultMessage) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)) {
                throw new IllegalStateException(getString(R.string.trigger_frame_jpeg_encode_failed));
            }
            byte[] jpeg = SurveyFrameExifWriter.INSTANCE.write(
                    output.toByteArray(), metadata,
                    new File(getCacheDir(), "trigger-frame-exif"));
            String timestamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss_SSS", Locale.US).format(
                    new Date(metadata.getCapturedAtEpochMillis()));
            String baseName = String.format(Locale.US, "TRIGGER_%s_%06d",
                    timestamp, metadata.getCapturedAtEpochMillis() % 1_000_000L);
            String directory = "trigger-frames/" + safeStorageName(
                    metadata.getMissionId() == null ? "manual" : metadata.getMissionId());
            String imagePath = savePublicDownloadFile(
                    directory, baseName + ".jpg", "image/jpeg", jpeg);
            JSONObject sidecar = metadata.toJson(imagePath)
                    .put("photo_result", resultMessage == null ? JSONObject.NULL : resultMessage);
            String metadataPath = savePublicDownloadFile(
                    directory, baseName + ".json", "application/json",
                    sidecar.toString(2).getBytes(StandardCharsets.UTF_8));
            writePersistentLogLine(String.format(Locale.US,
                    "%tF %<tT.%<tL  TRIGGER_FRAME image=%s metadata=%s reason=%s bytes=%d\n",
                    metadata.getCapturedAtEpochMillis(), imagePath, metadataPath,
                    metadata.getTriggerReason(), jpeg.length));
            V86StreamingController streaming = v86Controller;
            if (streaming != null && streaming.current().getSessionId() != null) {
                streaming.enqueueCaptureError(jpeg, metadata);
            }
        } catch (Throwable error) {
            Log.e(TAG, "save trigger-aligned frame failed", error);
            persistLogLine("TRIGGER_FRAME save failed: " + error.getMessage() + "\n");
        } finally {
            bitmap.recycle();
        }
    }

    private void captureSurveyUeFrame(long requestGeneration, String reason) {
        long requestedAtNanos = SystemClock.elapsedRealtimeNanos();
        pollSurveyUeFrame(requestGeneration, reason, requestedAtNanos,
                SystemClock.elapsedRealtime() + HIL_SURVEY_FRAME_WAIT_MILLIS);
    }

    private void pollSurveyUeFrame(long requestGeneration, String reason,
                                   long requestedAtNanos, long deadlineElapsedMs) {
        if (requestGeneration != surveyPhotoRequestGeneration || !surveyPhotoInFlight) return;
        AndroidHilController controller = hilController;
        HilFrameProtocol.Frame frame = controller == null
                ? null : controller.latestFrame(HIL_ASYNC_FRAME_FRESH_MILLIS);
        boolean isNewFrame = frame != null
                && frame.getReceivedAndroidMonotonicNanos() >= requestedAtNanos
                && frame.getReceivedAndroidMonotonicNanos() > lastSurveyUeCapturedReceivedNanos;
        if (isNewFrame) {
            saveSurveyUeFrame(requestGeneration, reason, frame);
            return;
        }
        if (SystemClock.elapsedRealtime() < deadlineElapsedMs) {
            mainHandler.postDelayed(() -> pollSurveyUeFrame(
                    requestGeneration, reason, requestedAtNanos, deadlineElapsedMs),
                    HIL_SURVEY_FRAME_POLL_MILLIS);
            return;
        }
        finishSurveyPhotoRequest(requestGeneration, false,
                getString(R.string.wait_next_ue_frame_timeout));
    }

    private void saveSurveyUeFrame(long requestGeneration, String reason,
                                   HilFrameProtocol.Frame frame) {
        SurveyMission mission = surveyMission;
        SurveySimulatorExecutionStateMachine execution = surveySimulatorExecution;
        if (mission == null || execution == null) {
            finishSurveyPhotoRequest(requestGeneration, false,
                    getString(R.string.route_execution_context_invalid));
            return;
        }
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        int executionLegIndex = execution.getExecutionLegIndex();
        int waypointIndex = execution.getStatus().getWaypointIndex();
        String format = frame.getFormat() == HilFrameProtocol.FORMAT_PNG ? "png" : "jpeg";
        String extension = frame.getFormat() == HilFrameProtocol.FORMAT_PNG ? ".png" : ".jpg";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        String baseName = String.format(Locale.US, "SURVEY_%s_%06d_%s",
                timestamp, frame.getFrameId(), format.toUpperCase(Locale.US));
        String relativeDirectory = "survey/" + safeStorageName(mission.getId());
        String captureEndpoint = resolvedSurveyUeEndpoint();
        storageExecutor.execute(() -> {
            try {
                String imagePath = savePublicDownloadFile(
                        relativeDirectory, baseName + extension,
                        frame.getFormat() == HilFrameProtocol.FORMAT_PNG ? "image/png" : "image/jpeg",
                        frame.getEncoded());
                SurveyUeCapture capture = new SurveyUeCapture(
                        System.currentTimeMillis(), mission.getId(), reason,
                        frame.getFrameId(), frame.getPoseSequence(), format,
                        frame.getWidth(), frame.getHeight(), frame.getCapturePeerMonotonicNanos(),
                        imagePath, snapshot.getLatitude(), snapshot.getLongitude(), snapshot.getAltitude(),
                        snapshot.getHeading(), snapshot.getGimbalPitch(), executionLegIndex, waypointIndex);
                byte[] metadata = SurveyUeBridgeContract.INSTANCE.encodeCapture(capture)
                        .getBytes(StandardCharsets.UTF_8);
                String metadataPath = savePublicDownloadFile(
                        relativeDirectory, baseName + ".json", "application/json", metadata);
                writePersistentLogLine(String.format(Locale.US,
                        "%tF %<tT.%<tL  SURVEY_FRAME image=%s metadata=%s frame=%d pose=%d reason=%s\n",
                        System.currentTimeMillis(), imagePath, metadataPath,
                        frame.getFrameId(), frame.getPoseSequence(), reason));
                ueBridgeClient.postCapture(captureEndpoint, capture, (ok, message) -> {
                    if (!ok) runOnUiThread(() -> appendLog("UE bridge capture FAIL " + message));
                });
                runOnUiThread(() -> {
                    lastSurveyUeCapturedReceivedNanos = frame.getReceivedAndroidMonotonicNanos();
                    finishSurveyPhotoRequest(requestGeneration, true,
                            getString(R.string.ue_frame_saved,
                                    frame.getFrameId(), frame.getPoseSequence()));
                });
            } catch (Throwable error) {
                Log.e(TAG, "save survey UE frame failed", error);
                runOnUiThread(() -> finishSurveyPhotoRequest(requestGeneration, false,
                        getString(R.string.ue_survey_image_save_failed, error.getMessage())));
            }
        });
    }

    private void finishSurveyPhotoRequest(long requestGeneration, boolean ok, String message) {
        if (requestGeneration != surveyPhotoRequestGeneration) {
            appendLog("SURVEY stale photo result ignored · " + (ok ? "OK" : "FAIL")
                    + " · " + message);
            return;
        }
        mainHandler.removeCallbacks(surveyPhotoTimeoutRunnable);
        surveyPhotoInFlight = false;
        surveyTriggerFrameId = 0L;
        surveyCaptureController.onCaptureResult(
                new GeoPoint(aircraftSnapshot.getLatitude(), aircraftSnapshot.getLongitude(),
                        aircraftSnapshot.getAltitude()),
                SystemClock.elapsedRealtime(), ok);
        int pointCaptureLegIndex = surveyPointCapturePendingLegIndex;
        if (pointCaptureLegIndex >= 0) {
            surveyPointCapturePendingLegIndex = -1;
            if (ok) surveyPointCaptureCompletedLegIndex = pointCaptureLegIndex;
        }
        appendLog("SURVEY photo result=" + (ok ? "OK" : "FAIL")
                + " latency_estimate="
                + surveyCaptureController.getEstimatedCaptureLatencyMillis() + "ms · " + message);
        showPhotoCaptureFeedback(ok);
        if (!ok && pointCaptureLegIndex >= 0) {
            pauseSurveyForRecoverableFault(getString(R.string.precise_recapture_failed, message), true);
        } else if (!ok && SurveyRuntimeFaultPolicy.isTimeout(message)) {
            pauseSurveyForRecoverableFault(getString(R.string.capture_command_timeout, message), true);
        } else if (!ok) {
                setSurveySimulatorStatus(getString(R.string.survey_capture_failure_warning), false);
        }
    }

    private void invalidateSurveyPhotoRequest() {
        if (surveyTriggerFrameId > 0L) {
            completeTriggerAlignedFrameCapture(
                    surveyTriggerFrameId, false, getString(R.string.capture_request_cancelled));
            surveyTriggerFrameId = 0L;
        }
        surveyPhotoRequestGeneration++;
        surveyPhotoInFlight = false;
        surveyPointCapturePendingLegIndex = -1;
        surveyCaptureController.cancelPendingCapture();
        mainHandler.removeCallbacks(surveyPhotoTimeoutRunnable);
    }

    private void appendSurveyMissionStartLog(SurveyExecutionGateResult gate) {
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        appendLog(String.format(Locale.US,
                "SURVEY mission start id=%s name=%s env=%s takeoff=%s mode=%s "
                        + "path=%.1fm duration=%.1fs photos=%d nadir=%.1fm/s oblique=%.1fm/s altitude=%.1fm "
                        + "safe=%.1fm start=%.1fm completion=%s headingMode=%s",
                surveyMission.getId(), surveyMission.getName(), surveyExecutionEnvironment,
                surveyMission.getConstraints().getTakeoffMode(),
                surveyMission.getConstraints().getCollectionMode(),
                surveyMission.getEstimatedPathMeters(), surveyMission.getEstimatedFlightSeconds(),
                surveyMission.getEstimatedPhotoCount(),
                surveyMission.getConstraints().getSpeedMetersPerSecond(),
                surveyMission.getConstraints().getObliqueSpeedMetersPerSecond(),
                surveyMission.getConstraints().getAltitudeMetersAgl(),
                surveyMission.getConstraints().getSafeTakeoffAltitudeMeters(),
                gate.getStartDistanceMeters(), surveyMission.getConstraints().getCompletionAction(),
                surveyMission.getConstraints().getObliqueHeadingMode()));
        appendLog(String.format(Locale.US,
                "SURVEY safety snapshot battery=%d%% rcBattery=%d%% rcSignal=%d%% satellites=%d "
                        + "gps=%s home=%s rth=%dm maxHeight=%dm radius=%dm radiusEnabled=%s",
                snapshot.getAircraftBattery(), snapshot.getRcBattery(), snapshot.getRcSignal(),
                snapshot.getSatellites(), snapshot.getGpsLevel(),
                Double.isFinite(snapshot.getHomeLatitude()) && Double.isFinite(snapshot.getHomeLongitude()),
                snapshot.getGoHomeHeightMeters(), snapshot.getMaxFlightHeightMeters(),
                snapshot.getMaxFlightRadiusMeters(), snapshot.getMaxFlightRadiusEnabled()));
    }

    private void appendSurveyFlightSample(SurveyExecutionStatus status) {
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        appendLog(String.format(Locale.US,
                "SURVEY telemetry state=%s phase=%s leg=%d/%d lat=%.7f lon=%.7f alt=%.2fm "
                        + "heading=%.1f hSpeed=%.2fm/s vSpeed=%+.2fm/s satellites=%d gps=%s "
                        + "battery=%d%% rcSignal=%d%% vs=%s sticks=%s",
                status.getState(), surveySimulatorExecution.getCurrentPhase(),
                surveySimulatorExecution.getExecutionLegIndex() + 1,
                surveySimulatorExecution.getExecutionLegCount(), snapshot.getLatitude(),
                snapshot.getLongitude(), snapshot.getAltitude(), snapshot.getHeading(),
                snapshot.getHorizontalSpeed(), snapshot.getVerticalSpeed(), snapshot.getSatellites(),
                snapshot.getGpsLevel(), snapshot.getAircraftBattery(), snapshot.getRcSignal(),
                snapshot.getVirtualStickEnabled(), snapshot.getSticksActive()));
    }

    private boolean isSurveySimulatorExecutionActive() {
        if (surveySimulatorExecution == null) return false;
        SurveyExecutionState state = surveySimulatorExecution.getStatus().getState();
        return state == SurveyExecutionState.ARMING || state == SurveyExecutionState.RUNNING;
    }

    private boolean isSurveySimulatorControlReserved() {
        return surveySimulatorExecution != null && SurveySimulatorSwitchPolicy.INSTANCE.reservesControl(
                surveySimulatorExecution.getStatus().getState());
    }

    private boolean isSurveyMissionLockedForEditing() {
        if (surveyTerrainCalculationInFlight) return true;
        if (surveySimulatorExecution == null) return false;
        SurveyExecutionState state = surveySimulatorExecution.getStatus().getState();
        return state == SurveyExecutionState.ARMING || state == SurveyExecutionState.RUNNING
                || state == SurveyExecutionState.PAUSED;
    }

    private boolean rejectSurveyEditingIfLocked() {
        if (!isSurveyMissionLockedForEditing()) return false;
        if (surveyTerrainCalculationInFlight) {
            renderSurveyStatus(getString(R.string.survey_edit_blocked_terrain_calculating));
            return true;
        }
        renderSurveyStatus(getString(R.string.survey_edit_locked_execution));
        showBanner(getString(R.string.survey_terminate_before_edit));
        return true;
    }

    private void renderSurveySimulatorGateFailure(String prefix, SurveyExecutionGateResult gate) {
        StringBuilder reasons = new StringBuilder();
        for (SurveyExecutionBlock block : gate.getBlocks()) {
            if (reasons.length() > 0) reasons.append(getString(R.string.list_separator));
            reasons.append(surveyBlockLabel(block));
        }
        setSurveySimulatorStatus(getString(R.string.survey_gate_no_control_sent,
                prefix, reasons.toString()), true);
    }

    private void renderSurveySimulatorExecutionStatus(
            SurveyExecutionStatus executionStatus, SurveyFollowerCommand command) {
        if (executionStatus == null) return;
        renderSurveyExecutionOverlay(false);
        renderSurveyEtaOverlay(executionStatus);
        String detail;
        if (executionStatus.getState() == SurveyExecutionState.RUNNING && surveyMission != null) {
            detail = getString(R.string.survey_execution_leg,
                    getString(surveyUiDryRunMode ? R.string.survey_dry_run : R.string.survey_running),
                    surveyExecutionPhaseLabel(surveySimulatorExecution.getCurrentPhase()),
                    surveySimulatorExecution.getExecutionLegIndex() + 1,
                    surveySimulatorExecution.getExecutionLegCount());
            if (command != null) {
                detail += getString(R.string.survey_execution_error,
                        command.getHorizontalErrorMeters(), command.getVerticalErrorMeters());
            }
            detail += surveyRemainingTimeLabel();
            if (surveyUiDryRunMode) detail += " · NO CONTROL";
        } else if (executionStatus.getState() == SurveyExecutionState.PAUSED) {
            detail = getString(surveyUiDryRunMode
                    ? R.string.survey_paused_dry : R.string.survey_paused_safe);
            if (executionStatus.getReason() != null
                    && !executionStatus.getReason().trim().isEmpty()) {
                detail += " · " + executionStatus.getReason();
            }
            detail += surveyRemainingTimeLabel();
        } else if (executionStatus.getState() == SurveyExecutionState.COMPLETED) {
            if (surveyUiDryRunMode) {
                detail = "DRY RUN COMPLETED · NO CONTROL · NO CAMERA";
            } else {
                detail = surveyMission != null && surveyMission.getConstraints().getCompletionAction()
                        == SurveyCompletionAction.RETURN_TO_HOME
                        ? getString(R.string.survey_completed_home)
                        : getString(R.string.survey_completed_release);
            }
        } else if (executionStatus.getState() == SurveyExecutionState.ABORTED) {
            detail = (surveyUiDryRunMode ? "DRY RUN ABORTED · NO CONTROL · " : "ABORTED · ")
                    + (executionStatus.getReason() == null
                    ? getString(R.string.survey_abort_safe) : executionStatus.getReason());
        } else {
            detail = executionStatus.getState().name();
        }
        boolean danger = executionStatus.getState() == SurveyExecutionState.ABORTED;
        setSurveySimulatorStatus(detail, danger);
        Button start = findViewById(R.id.survey_sim_start_button);
        if (start != null) start.setText(executionStatus.getState() == SurveyExecutionState.PAUSED
                ? R.string.resume_route_execution : R.string.start_route_execution);
        Button plannerExecute = findViewById(R.id.survey_planner_execute_button);
        if (plannerExecute != null) plannerExecute.setText(
                executionStatus.getState() == SurveyExecutionState.PAUSED
                        ? R.string.action_continue : R.string.action_execute);
        Button headerPause = findViewById(R.id.survey_header_pause_button);
        if (headerPause != null) {
            boolean canPause = executionStatus.getState() == SurveyExecutionState.RUNNING
                    || executionStatus.getState() == SurveyExecutionState.PAUSED;
            headerPause.setText(executionStatus.getState() == SurveyExecutionState.PAUSED
                    ? R.string.survey_continue : R.string.survey_pause);
            headerPause.setEnabled(canPause);
            headerPause.setVisibility(canPause ? View.VISIBLE : View.GONE);
        }
        View headerAbort = findViewById(R.id.survey_planner_abort_button);
        if (headerAbort != null) {
            boolean canAbort = executionStatus.getState() == SurveyExecutionState.ARMING
                    || executionStatus.getState() == SurveyExecutionState.RUNNING
                    || executionStatus.getState() == SurveyExecutionState.PAUSED;
            headerAbort.setVisibility(canAbort ? View.VISIBLE : View.GONE);
        }
        renderSurveyEtaBadge();
    }

    private String surveyRemainingTimeLabel() {
        SurveyRemainingEstimate estimate = currentSurveyRemainingEstimate();
        if (estimate == null) return "";
        String sectionName = getString(surveySimulatorExecution.getCurrentPhase() == SurveyExecutionPhase.SURVEY
                ? R.string.survey_remaining_section : R.string.survey_remaining_phase);
        return getString(R.string.survey_remaining_summary, sectionName,
                formatDuration((int) Math.ceil(estimate.getCurrentSectionSeconds())),
                formatDuration((int) Math.ceil(estimate.getTotalSeconds())));
    }

    private SurveyRemainingEstimate currentSurveyRemainingEstimate() {
        if (surveySimulatorExecution == null) return null;
        GeoPoint livePosition = null;
        if (Double.isFinite(aircraftSnapshot.getLatitude())
                && Double.isFinite(aircraftSnapshot.getLongitude())
                && Double.isFinite(aircraftSnapshot.getAltitude())) {
            livePosition = new GeoPoint(
                    aircraftSnapshot.getLatitude(),
                    aircraftSnapshot.getLongitude(),
                    aircraftSnapshot.getAltitude());
        }
        return surveySimulatorExecution.remainingEstimate(
                livePosition,
                aircraftSnapshot.getHeading(),
                aircraftSnapshot.getHorizontalSpeed(),
                aircraftSnapshot.getVerticalSpeed());
    }

    private SurveyRemainingEstimate plannedSurveyRemainingEstimate() {
        if (surveyMission == null || surveyMission.getWaypoints().isEmpty()) return null;
        if (surveyPlannedEtaSource != surveyMission || surveyPlannedEta == null) {
            SurveySimulatorExecutionStateMachine estimator =
                    new SurveySimulatorExecutionStateMachine(surveyMission, null, null);
            surveyPlannedEta = estimator.remainingEstimate(
                    surveyMission.getWaypoints().get(0).getPoint(),
                    Double.NaN, Double.NaN, Double.NaN);
            surveyPlannedEtaSource = surveyMission;
        }
        return surveyPlannedEta;
    }

    private void renderSurveyEtaBadge() {
        TextView badge = findViewById(R.id.survey_eta_badge);
        if (badge == null) return;
        if (surveyMission == null) {
            badge.setVisibility(View.GONE);
            return;
        }
        SurveyExecutionState state = surveySimulatorExecution == null
                ? SurveyExecutionState.IDLE : surveySimulatorExecution.getStatus().getState();
        if (state == SurveyExecutionState.COMPLETED) {
            badge.setText(R.string.survey_eta_completed_short);
        } else {
            boolean active = state == SurveyExecutionState.ARMING
                    || state == SurveyExecutionState.RUNNING
                    || state == SurveyExecutionState.PAUSED;
            SurveyRemainingEstimate estimate = active
                    ? currentSurveyRemainingEstimate() : plannedSurveyRemainingEstimate();
            if (estimate == null) {
                badge.setVisibility(View.GONE);
                return;
            }
            int label = state == SurveyExecutionState.PAUSED
                    ? R.string.survey_eta_paused_short
                    : active ? R.string.survey_eta_remaining_short
                    : R.string.survey_eta_planned_short;
            badge.setText(getString(label,
                    formatDuration((int) Math.ceil(estimate.getTotalSeconds()))));
        }
        badge.setVisibility(View.VISIBLE);
    }

    private void renderSurveyEtaOverlay(SurveyExecutionStatus executionStatus) {
        View overlay = findViewById(R.id.survey_eta_overlay);
        if (overlay == null) return;
        SurveyExecutionState state = executionStatus.getState();
        boolean visible = surveySimulatorExecution != null && surveyMission != null
                && (state == SurveyExecutionState.ARMING
                || state == SurveyExecutionState.RUNNING
                || state == SurveyExecutionState.PAUSED);
        overlay.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) return;
        SurveyRemainingEstimate estimate = currentSurveyRemainingEstimate();
        if (estimate == null) return;
        SurveyExecutionPhase phase = surveySimulatorExecution.getCurrentPhase();
        String sectionName = getString(phase == SurveyExecutionPhase.SURVEY
                ? R.string.survey_remaining_section : R.string.survey_remaining_phase);
        TextView title = findViewById(R.id.survey_eta_title);
        TextView time = findViewById(R.id.survey_eta_time);
        TextView detail = findViewById(R.id.survey_eta_detail);
        if (title != null) {
            title.setText(getString(state == SurveyExecutionState.PAUSED
                    ? R.string.survey_eta_paused : R.string.survey_eta_executing)
                    + " · " + surveyExecutionPhaseLabel(phase)
                    + " · " + (surveySimulatorExecution.getExecutionLegIndex() + 1)
                    + "/" + surveySimulatorExecution.getExecutionLegCount());
        }
        if (time != null) {
            time.setText(getString(R.string.survey_eta_time_summary, sectionName,
                    formatDuration((int) Math.ceil(estimate.getCurrentSectionSeconds())),
                    formatDuration((int) Math.ceil(estimate.getTotalSeconds()))));
        }
        if (detail != null) {
            String pauseReason = executionStatus.getReason();
            if (state == SurveyExecutionState.PAUSED
                    && pauseReason != null && !pauseReason.trim().isEmpty()) {
                detail.setText(getString(R.string.survey_pause_reason, pauseReason));
                detail.setTextColor(getColor(R.color.text_danger));
            } else {
                detail.setText(getString(R.string.survey_eta_speed_detail,
                        aircraftSnapshot.getHorizontalSpeed(), aircraftSnapshot.getVerticalSpeed()));
                detail.setTextColor(Color.parseColor("#FF66717D"));
            }
        }
    }

    private String surveyExecutionPhaseLabel(
            edu.playground.djivln.survey.SurveyExecutionPhase phase) {
        switch (phase) {
            case SAFE_CLIMB: return getString(R.string.phase_safe_climb);
            case TRANSIT_TO_START: return getString(R.string.phase_transit_to_start);
            case RECOVERY_TO_PAUSE: return getString(R.string.phase_recovery_to_pause);
            case RETURN_HOME: return getString(R.string.phase_return_home);
            case RETURN_TO_START: return getString(R.string.phase_return_route_start);
            case SURVEY:
            default: return getString(R.string.phase_survey_capture);
        }
    }

    private void setSurveySimulatorStatus(String text, boolean danger) {
        int color = getColor(danger ? R.color.text_danger : R.color.text_ok_green);
        setSurveySimulatorStatusColor(text, color);
    }

    private void setSurveySimulatorStatusColor(String text, int color) {
        TextView view = findViewById(R.id.survey_sim_gate_status);
        if (view != null) {
            view.setText(text);
            view.setTextColor(color);
        }
        TextView summary = findViewById(R.id.survey_execution_status_text);
        if (summary != null) {
            String compact = text.replace('\n', ' ')
                    .replace("  ", " ")
                    .trim();
            if (compact.length() > 52) {
                compact = getString(R.string.compact_status_tap_to_view,
                        compact.substring(0, 49));
            }
            summary.setText(compact);
            summary.setTextColor(color);
        }
    }

    private void persistSurveyMission(boolean resetCheckpoint) {
        if (surveyMission == null) return;
        android.content.SharedPreferences.Editor editor = getSharedPreferences(
                SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                .putString(SURVEY_MISSION_KEY, SurveyMissionJson.INSTANCE.encode(surveyMission));
        if (activeRecaptureSourceMission != null) {
            editor.putString(SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY,
                    SurveyMissionJson.INSTANCE.encode(activeRecaptureSourceMission));
        } else {
            editor.remove(SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY);
        }
        if (resetCheckpoint) editor.remove(SURVEY_CHECKPOINT_KEY);
        editor.apply();
    }

    private void persistSurveyCheckpoint(SurveyExecutionStatus executionStatus) {
        if (surveyMission == null || executionStatus == null) return;
        SurveyExecutionState state = executionStatus.getState();
        if (state != SurveyExecutionState.RUNNING && state != SurveyExecutionState.PAUSED
                && state != SurveyExecutionState.ARMING) {
            clearPersistedSurveyCheckpoint();
            return;
        }
        SurveyRecoveryPosition recovery = SurveyCheckpointRecoveryPolicy.INSTANCE.position(
                executionStatus.getWaypointIndex(), surveySimulatorExecution.getExecutionLegIndex(),
                state, surveySimulatorExecution.getCurrentPhase(),
                surveySimulatorExecution.getCurrentTarget().getCaptureAction(),
                surveySimulatorExecution.pausedRecoveryPoint() != null);
        SurveyExecutionCheckpoint checkpoint = new SurveyExecutionCheckpoint(
                surveyMission.getId(), recovery.getWaypointIndex(), state,
                System.currentTimeMillis(), recovery.getExecutionLegIndex(),
                surveySimulatorExecution.getCurrentPhase(),
                surveySimulatorExecution.pausedRecoveryPoint());
        android.content.SharedPreferences.Editor editor =
                getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                .putString(SURVEY_MISSION_KEY, SurveyMissionJson.INSTANCE.encode(surveyMission))
                .putString(SURVEY_CHECKPOINT_KEY,
                        SurveyExecutionCheckpointJson.INSTANCE.encode(checkpoint));
        if (activeRecaptureSourceMission != null) {
            editor.putString(SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY,
                    SurveyMissionJson.INSTANCE.encode(activeRecaptureSourceMission));
        } else {
            editor.remove(SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY);
        }
        editor.apply();
    }

    private void clearPersistedSurveyCheckpoint() {
        getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                .remove(SURVEY_CHECKPOINT_KEY).apply();
    }

    private void clearPersistedSurveySession() {
        getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                .remove(SURVEY_MISSION_KEY)
                .remove(SURVEY_CHECKPOINT_KEY)
                .remove(SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY)
                .apply();
        activeRecaptureSourceMission = null;
    }

    private List<SurveyMissionVersion> surveyMissionVersions() {
        String raw = getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE)
                .getString(SURVEY_LIBRARY_KEY, null);
        try {
            return SurveyMissionLibrary.INSTANCE.decode(raw);
        } catch (Throwable error) {
            appendLog("SURVEY library ignored invalid data: " + error.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveSurveyMissionVersion(boolean announce) {
        if (surveyMission == null) {
            if (announce) renderSurveyStatus(getString(R.string.survey_save_version_requires_mission));
            return;
        }
        List<SurveyMissionVersion> versions = SurveyMissionLibrary.INSTANCE.addVersion(
                surveyMissionVersions(), surveyMission, System.currentTimeMillis());
        getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                .putString(SURVEY_LIBRARY_KEY, SurveyMissionLibrary.INSTANCE.encode(versions))
                .apply();
        SurveyMissionVersion saved = versions.get(0);
        appendLog("SURVEY library saved " + saved.getMissionName() + " v" + saved.getRevision());
        if (announce) renderSurveyStatus(getString(R.string.survey_version_saved,
                saved.getMissionName(), saved.getRevision(), surveySummary(surveyMission)));
    }

    private void showSurveyMissionLibrary() {
        List<SurveyMissionVersion> versions = surveyMissionVersions();
        if (versions.isEmpty()) {
            renderSurveyStatus(getString(R.string.survey_library_empty));
            return;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
        String[] labels = new String[versions.size()];
        for (int index = 0; index < versions.size(); index++) {
            SurveyMissionVersion version = versions.get(index);
            labels[index] = version.getMissionName() + "  v" + version.getRevision()
                    + "  ·  " + formatter.format(new Date(version.getSavedAtEpochMillis()));
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.survey_library_title, versions.size()))
                .setItems(labels, (dialog, which) -> {
                    try {
                        activateSurveyMission(versions.get(which).mission(),
                                getString(R.string.restored_named_mission, labels[which]));
                    } catch (Throwable error) {
                        renderSurveyStatus(getString(R.string.survey_version_restore_failed,
                                error.getMessage()));
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void activateSurveyMission(SurveyMission mission, String statusPrefix) {
        abortSurveySimulatorExecution(getString(R.string.reason_switch_survey_mission), true);
        stopSurveyReplay(true);
        surveyMission = mission;
        activeRecaptureSourceMission = mission.getActiveMapping() == null ? null : mission;
        setSurveyTerrainFollowingEnabled(mission.getTerrainPlan() != null, false);
        selectedSurveyVertexIndex = -1;
        persistSurveyMission(true);
        surveyRoi.clear();
        surveyRoi.addAll(mission.getRoi());
        ((EditText) findViewById(R.id.survey_name_input)).setText(mission.getName());
        ((EditText) findViewById(R.id.survey_altitude_input)).setText(
                String.format(Locale.US, "%.0f", mission.getConstraints().getAltitudeMetersAgl()));
        ((EditText) findViewById(R.id.survey_heading_input)).setText(
                String.format(Locale.US, "%.0f", mission.getConstraints().getRouteHeadingDegrees()));
        renderSurveyConstraintInputs(mission.getConstraints());
        showSurveyPlanner(true);
        frameSurveyMission(mission);
        renderSurveyOverlay();
        ((TerrainPreviewView) findViewById(R.id.survey_terrain_preview)).showMission(mission);
        renderSurveyStatus(statusPrefix + " · " + surveySummary(mission));
        appendLog("SURVEY activated " + mission.getId());
    }

    private void restoreSurveySession() {
        android.content.SharedPreferences preferences = getSharedPreferences(
                SURVEY_SESSION_PREFERENCES, MODE_PRIVATE);
        String missionRaw = preferences.getString(SURVEY_MISSION_KEY, null);
        if (missionRaw == null) return;
        try {
            SurveyMission restoredMission = SurveyMissionJson.INSTANCE.decode(missionRaw);
            String normalizedMissionJson = SurveyMissionJson.INSTANCE.encode(restoredMission);
            if (!normalizedMissionJson.equals(missionRaw)) {
                preferences.edit().putString(SURVEY_MISSION_KEY, normalizedMissionJson).apply();
                appendLog("SURVEY mission schema migrated · headingMode="
                        + restoredMission.getConstraints().getObliqueHeadingMode());
            }
            surveyMission = restoredMission;
            activeRecaptureSourceMission = null;
            if (restoredMission.getActiveMapping() != null) {
                String sourceRaw = preferences.getString(
                        SURVEY_ACTIVE_RECAPTURE_SOURCE_MISSION_KEY, null);
                if (sourceRaw != null) {
                    try {
                        SurveyMission restoredSource = SurveyMissionJson.INSTANCE.decode(sourceRaw);
                        activeRecaptureSourceMission = restoredSource;
                        if (!activeRecaptureSourceContains(restoredMission)) {
                            activeRecaptureSourceMission = restoredMission;
                        }
                    } catch (Throwable sourceError) {
                        appendLog("SURVEY active recapture source ignored: "
                                + sourceError.getMessage());
                    }
                }
                if (activeRecaptureSourceMission == null) {
                    activeRecaptureSourceMission = restoredMission;
                }
            }
            setSurveyTerrainFollowingEnabled(restoredMission.getTerrainPlan() != null, false);
            surveyRoi.clear();
            surveyRoi.addAll(restoredMission.getRoi());
            ((EditText) findViewById(R.id.survey_altitude_input)).setText(String.format(
                    Locale.US, "%.0f", restoredMission.getConstraints().getAltitudeMetersAgl()));
            ((EditText) findViewById(R.id.survey_name_input)).setText(restoredMission.getName());
            ((EditText) findViewById(R.id.survey_heading_input)).setText(String.format(
                    Locale.US, "%.0f", restoredMission.getConstraints().getRouteHeadingDegrees()));
            renderSurveyConstraintInputs(restoredMission.getConstraints());
            ((TerrainPreviewView) findViewById(R.id.survey_terrain_preview)).showMission(restoredMission);

            String checkpointRaw = preferences.getString(SURVEY_CHECKPOINT_KEY, null);
            if (checkpointRaw != null) {
                SurveyExecutionCheckpoint checkpoint =
                        SurveyExecutionCheckpointJson.INSTANCE.decode(checkpointRaw);
                if (checkpoint.getMissionId().equals(restoredMission.getId())
                        && checkpoint.getWaypointIndex() < restoredMission.getWaypoints().size()
                        && (checkpoint.getState() == SurveyExecutionState.RUNNING
                        || checkpoint.getState() == SurveyExecutionState.ARMING
                        || checkpoint.getState() == SurveyExecutionState.PAUSED)) {
                    surveySimulatorExecution = new SurveySimulatorExecutionStateMachine(
                            restoredMission, currentAircraftGeoPoint(),
                            currentSurveySimulatorReturnPoint());
                    SurveyExecutionStatus paused = surveySimulatorExecution.restorePaused(
                            checkpoint.getWaypointIndex(), checkpoint.getExecutionLegIndex(),
                            checkpoint.getRecoveryPoint());
                    if (checkpoint.getPhase()
                            == edu.playground.djivln.survey.SurveyExecutionPhase.SURVEY) {
                        restoreSurveyCaptureState(checkpoint.getWaypointIndex());
                    } else {
                        surveyCaptureController.reset();
                    }
                    renderSurveySimulatorExecutionStatus(paused, null);
                    appendLog("SURVEY restored PAUSED waypoint="
                            + (checkpoint.getWaypointIndex() + 1) + " · NO_CONTROL");
                } else {
                    clearPersistedSurveyCheckpoint();
                }
            }
            renderSurveyOverlay();
            renderSurveyStatus(getString(R.string.survey_mission_restored,
                    surveySummary(restoredMission)));
            appendLog("SURVEY mission restored " + restoredMission.getId());
        } catch (Throwable error) {
            clearPersistedSurveySession();
            surveyMission = null;
            activeRecaptureSourceMission = null;
            surveyRoi.clear();
            appendLog("SURVEY restore discarded invalid session: " + error.getMessage());
        }
    }

    private boolean activeRecaptureSourceContains(SurveyMission selected) {
        if (activeRecaptureSourceMission == null
                || activeRecaptureSourceMission.getActiveMapping() == null
                || selected == null || selected.getActiveMapping() == null) {
            return false;
        }
        Set<String> available = new HashSet<>();
        activeRecaptureSourceMission.getActiveMapping().getRegions().forEach(
                region -> available.add(region.getRegionId()));
        for (edu.playground.djivln.survey.ActiveMappingRegionMetadata region
                : selected.getActiveMapping().getRegions()) {
            if (!available.contains(region.getRegionId())) return false;
        }
        return true;
    }

    private void restoreSurveyCaptureState(int waypointIndex) {
        surveyCaptureController.configure(
                surveyMission.getConstraints().getCaptureTriggerMode(),
                surveyMission.getConstraints().getTimedCaptureIntervalSeconds());
        Double activeInterval = null;
        for (int index = 0; index < waypointIndex; index++) {
            SurveyWaypoint waypoint = surveyMission.getWaypoints().get(index);
            switch (waypoint.getCaptureAction()) {
                case START_DISTANCE_INTERVAL:
                    activeInterval = waypoint.getCaptureIntervalMeters();
                    break;
                case STOP_DISTANCE_INTERVAL:
                    activeInterval = null;
                    break;
                default:
                    break;
            }
        }
        if (activeInterval != null) surveyCaptureController.restoreActive(
                activeInterval,
                surveyMission.getConstraints().getCaptureTriggerMode(),
                surveyMission.getConstraints().getTimedCaptureIntervalSeconds());
    }

    private void renderSurveyConstraintInputs(SurveyConstraints constraints) {
        surveySpeedHintFiveDirection = constraints.getCollectionMode()
                == SurveyCollectionMode.OBLIQUE_FIVE_DIRECTION;
        surveyEnabledCaptureViews.clear();
        for (SurveyCaptureView view : constraints.getEnabledCaptureViews()) {
            if (view != SurveyCaptureView.LOCAL_OBLIQUE) surveyEnabledCaptureViews.add(view);
        }
        if (surveyEnabledCaptureViews.isEmpty()) {
            surveyEnabledCaptureViews.addAll(EnumSet.of(SurveyCaptureView.NADIR,
                    SurveyCaptureView.FORWARD_OBLIQUE, SurveyCaptureView.BACKWARD_OBLIQUE,
                    SurveyCaptureView.LEFT_OBLIQUE, SurveyCaptureView.RIGHT_OBLIQUE));
        }
        renderSurveyRoutePreviewStyles();
        ((EditText) findViewById(R.id.survey_forward_overlap_input)).setText(String.format(
                Locale.US, "%.0f", constraints.getForwardOverlap() * 100.0));
        ((EditText) findViewById(R.id.survey_side_overlap_input)).setText(String.format(
                Locale.US, "%.0f", constraints.getSideOverlap() * 100.0));
        ((EditText) findViewById(R.id.survey_speed_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getSpeedMetersPerSecond()));
        ((EditText) findViewById(R.id.survey_oblique_speed_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getObliqueSpeedMetersPerSecond()));
        ((EditText) findViewById(R.id.survey_gimbal_input)).setText(String.format(
                Locale.US, "%.0f", constraints.getObliqueGimbalPitchDegrees()));
        ((EditText) findViewById(R.id.survey_margin_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getBoundaryMarginMeters()));
        ((EditText) findViewById(R.id.survey_target_offset_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getTargetSurfaceToTakeoffMeters()));
        ((EditText) findViewById(R.id.survey_safe_takeoff_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getSafeTakeoffAltitudeMeters()));
        ((EditText) findViewById(R.id.survey_takeoff_speed_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getTakeoffSpeedMetersPerSecond()));
        ((EditText) findViewById(R.id.survey_oblique_forward_overlap_input)).setText(String.format(
                Locale.US, "%.0f", constraints.getObliqueForwardOverlap() * 100.0));
        ((EditText) findViewById(R.id.survey_oblique_side_overlap_input)).setText(String.format(
                Locale.US, "%.0f", constraints.getObliqueSideOverlap() * 100.0));
        ((Spinner) findViewById(R.id.survey_altitude_mode_spinner)).setSelection(
                constraints.getAltitudeMode() == SurveyAltitudeMode.RELATIVE_TO_TAKEOFF ? 1 : 0);
        ((Spinner) findViewById(R.id.survey_takeoff_mode_spinner)).setSelection(
                constraints.getTakeoffMode() == SurveyTakeoffMode.AUTO_SIMULATOR_ONLY ? 1 : 0);
        ((Spinner) findViewById(R.id.survey_start_mode_spinner)).setSelection(
                surveyStartModeSelection(constraints.getStartPointMode()));
        ((Spinner) findViewById(R.id.survey_completion_spinner)).setSelection(
                constraints.getCompletionAction() == SurveyCompletionAction.HOVER ? 1
                        : constraints.getCompletionAction() == SurveyCompletionAction.RETURN_TO_ROUTE_START ? 2 : 0);
        ((Spinner) findViewById(R.id.survey_oblique_heading_mode_spinner)).setSelection(
                constraints.getObliqueHeadingMode()
                        == SurveyObliqueHeadingMode.FIXED_CAPTURE_DIRECTION ? 1 : 0);
        ((Spinner) findViewById(R.id.survey_capture_mode_spinner)).setSelection(
                constraints.getCaptureTriggerMode() == SurveyCaptureTriggerMode.TIME ? 1 : 0);
        ((EditText) findViewById(R.id.survey_timed_interval_input)).setText(String.format(
                Locale.US, "%.1f", constraints.getTimedCaptureIntervalSeconds()));
    }

    private int surveyStartModeSelection(SurveyStartPointMode mode) {
        switch (mode) {
            case FIRST_ROUTE_START: return 1;
            case ROUTE_CORNER_2: return 2;
            case ROUTE_CORNER_3: return 3;
            case ROUTE_CORNER_4: return 4;
            case AUTO_NEAREST:
            case CUSTOM:
            default: return 0;
        }
    }

    private void renderSurveyStatus(String text) {
        TextView status = findViewById(R.id.survey_status_text);
        if (status != null) status.setText(text);
        renderSurveyStatistics();
    }

    private void renderSurveyStatistics() {
        TextView statisticsView = findViewById(R.id.survey_statistics_text);
        if (statisticsView == null) return;
        if (surveyMission == null) {
            statisticsView.setText(getString(R.string.survey_summary_placeholder));
            return;
        }
        SurveyPlanner.MissionStatistics statistics = SurveyPlanner.INSTANCE.statistics(
                surveyMission, 5.0, 20.0 * 60.0, currentSurveyTakeoffPoint());
        int nadirPhotos = statistics.getPhotoCountByView().getOrDefault(
                SurveyCaptureView.NADIR, 0);
        int obliquePhotos = 0;
        for (SurveyCaptureView view : SurveyCaptureView.values()) {
            if (view != SurveyCaptureView.NADIR) {
                obliquePhotos += statistics.getPhotoCountByView().getOrDefault(view, 0);
            }
        }
        double storage = statistics.getEstimatedStorageMegabytes();
        String storageLabel = storage >= 1024.0
                ? String.format(Locale.US, "%.1f GB", storage / 1024.0)
                : String.format(Locale.US, "%.0f MB", storage);
        StringBuilder sortieRanges = new StringBuilder();
        if (statistics.getSorties().size() > 1) {
            int shown = Math.min(3, statistics.getSorties().size());
            for (int index = 0; index < shown; index++) {
                SurveyPlanner.SurveySortie sortie = statistics.getSorties().get(index);
                if (sortieRanges.length() > 0) sortieRanges.append(" / ");
                sortieRanges.append(getString(R.string.survey_sortie_waypoint_range,
                        index + 1, sortie.getFirstWaypointIndex() + 1,
                        sortie.getLastWaypointIndex() + 1));
            }
            if (statistics.getSorties().size() > shown) sortieRanges.append(" / …");
        }
        statisticsView.setText(getString(R.string.survey_statistics,
                nadirPhotos + obliquePhotos, nadirPhotos, obliquePhotos, storageLabel,
                statistics.getEstimatedSorties(),
                statistics.getOperationalReferenceAvailable()
                        ? getString(R.string.survey_duration_approx,
                        statistics.getTransitAndCompletionSeconds() / 60.0)
                        : getString(R.string.survey_duration_waiting_gps),
                sortieRanges.length() == 0 ? "" : " · " + sortieRanges));
    }

    private void initHilUi() {
        android.content.SharedPreferences preferences = getSharedPreferences("ue-hil", MODE_PRIVATE);
        EditText host = findViewById(R.id.hil_host);
        EditText udpPort = findViewById(R.id.hil_udp_port);
        EditText framePort = findViewById(R.id.hil_frame_port);
        host.setText(preferences.getString("host", "192.168.1.2"));
        udpPort.setText(String.valueOf(preferences.getInt("udp-port", 30_020)));
        framePort.setText(String.valueOf(preferences.getInt("frame-port", 30_022)));
        int[] frequencies = new int[] {10, 25, 50, 100, 150};
        String[] labels = new String[] {"10 Hz", "25 Hz", "50 Hz", "100 Hz", "150 Hz"};
        hilSimulatorStateHz = preferences.getInt("simulator-hz", 100);
        try {
            hilConnectionMode = HilConnectionMode.valueOf(
                    preferences.getString("connection-mode", HilConnectionMode.HOTSPOT.name()));
        } catch (IllegalArgumentException ignored) {
            hilConnectionMode = HilConnectionMode.HOTSPOT;
        }
        Spinner spinner = findViewById(R.id.hil_simulator_rate_spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, R.layout.item_prompt_spinner, labels));
        int selected = 3;
        for (int index = 0; index < frequencies.length; index++) {
            if (frequencies[index] == hilSimulatorStateHz) selected = index;
        }
        spinner.setSelection(selected, false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int value = frequencies[Math.max(0, Math.min(position, frequencies.length - 1))];
                if (value == hilSimulatorStateHz) return;
                hilSimulatorStateHz = value;
                preferences.edit().putInt("simulator-hz", value).apply();
                if (aircraftBridge != null) aircraftBridge.setSimulatorUpdateFrequencyHz(value);
                if (hilController != null && hilController.isRunning()) {
                    if (hasActiveHilControlledOperation()) {
                        normalStop(getString(R.string.reason_adjust_hil_simulator_frequency));
                    }
                    hilVirtualFramesEnabled = false;
                    stopHilLink(getString(R.string.reason_adjust_hil_simulator_frequency), false);
                    appendLog("HIL frequency changed to " + value + " Hz; link stopped for clean restart");
                }
                renderHilStatus();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        findViewById(R.id.hil_mode_hotspot).setOnClickListener(v ->
                selectHilConnectionMode(HilConnectionMode.HOTSPOT));
        findViewById(R.id.hil_mode_lan).setOnClickListener(v ->
                selectHilConnectionMode(HilConnectionMode.LAN));
        findViewById(R.id.hil_open_hotspot_settings).setOnClickListener(v -> openSystemHotspotSettings());
        findViewById(R.id.hil_link_toggle).setOnClickListener(v -> toggleHilLink());
        findViewById(R.id.hil_frame_toggle).setOnClickListener(v -> toggleHilVirtualFrames());
        findViewById(R.id.hil_frame_preview).setOnClickListener(v -> previewLatestHilFrame());
        renderHilConnectionMode();
        renderHilStatus();
    }

    private void selectHilConnectionMode(HilConnectionMode mode) {
        if (mode == hilConnectionMode) return;
        if (hilController != null && hilController.isRunning()) {
            if (hasActiveHilControlledOperation()) normalStop(getString(R.string.reason_switch_hil_network_mode));
            hilVirtualFramesEnabled = false;
            stopHilLink(getString(R.string.reason_switch_hil_network_mode), false);
        }
        hilConnectionMode = mode;
        getSharedPreferences("ue-hil", MODE_PRIVATE).edit()
                .putString("connection-mode", mode.name()).apply();
        appendLog("HIL connection mode=" + mode.name());
        renderHilConnectionMode();
        renderHilStatus();
    }

    private void renderHilConnectionMode() {
        Button hotspot = findViewById(R.id.hil_mode_hotspot);
        Button lan = findViewById(R.id.hil_mode_lan);
        boolean direct = hilConnectionMode == HilConnectionMode.HOTSPOT;
        findViewById(R.id.hil_open_hotspot_settings).setVisibility(direct ? View.VISIBLE : View.GONE);
        findViewById(R.id.hil_lan_host_container).setVisibility(direct ? View.GONE : View.VISIBLE);
        TextView hint = findViewById(R.id.hil_network_hint);
        hint.setText(direct ? R.string.hil_hotspot_setup_help : R.string.hil_lan_setup_help);
        styleButton(hotspot,
                direct ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                direct ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
        styleButton(lan,
                !direct ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                !direct ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
    }

    private void openSystemHotspotSettings() {
        try {
            startActivity(new Intent("android.settings.TETHER_SETTINGS"));
        } catch (Throwable error) {
            try {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            } catch (Throwable fallbackError) {
                showBanner(getString(R.string.hil_hotspot_settings_failed));
            }
        }
    }

    private void ensureHilController() {
        if (hilController != null) return;
        hilController = new AndroidHilController(new AndroidHilController.Listener() {
            @Override public void onHilEvent(HilProtocol.Event event) {
                mainHandler.post(() -> handleHilEvent(event));
            }

            @Override public void onHilLinkStale() {
                mainHandler.post(() -> {
                    appendLog("HIL heartbeat timeout");
                    if (hilVirtualFramesEnabled && hasActiveHilControlledOperation()) {
                        hilVirtualFramesEnabled = false;
                        normalStop(getString(R.string.ue_hil_heartbeat_timeout));
                    }
                    renderHilStatus();
                    renderSafetyState();
                });
            }

            @Override public void onHilStatus(AndroidHilController.Status status) {
                latestHilStatus = status;
                mainHandler.post(() -> {
                    monitorHilVirtualFrameAvailability(status);
                    renderHilStatus();
                });
            }

            @Override public void onHilError(String message) {
                mainHandler.post(() -> {
                    appendLog("HIL " + message);
                    renderHilStatus();
                });
            }
        }, this);
    }

    private void toggleHilLink() {
        ensureHilController();
        if (hilController.isRunning()) {
            if (hasActiveHilControlledOperation()) normalStop(getString(R.string.reason_close_ue_hil_link));
            hilVirtualFramesEnabled = false;
            stopHilLink(getString(R.string.reason_close_ue_hil_link), false);
            appendLog("HIL link stopped");
            renderHilStatus();
            return;
        }
        String host = ((EditText) findViewById(R.id.hil_host)).getText().toString().trim();
        int udpPort;
        int framePort;
        try {
            udpPort = parseHilPort(R.id.hil_udp_port, 30_020);
            framePort = parseHilPort(R.id.hil_frame_port, 30_022);
            if (hilConnectionMode == HilConnectionMode.LAN && host.isEmpty()) {
                throw new IllegalArgumentException(getString(R.string.hil_lan_requires_ue_ip));
            }
            AndroidHilController.Config config = new AndroidHilController.Config(
                    hilConnectionMode, host, udpPort, 30_021, framePort, hilSimulatorStateHz,
                    hilSimulatorStateHz, 1_000L);
            getSharedPreferences("ue-hil", MODE_PRIVATE).edit()
                    .putString("host", host)
                    .putInt("udp-port", udpPort)
                    .putInt("frame-port", framePort)
                    .putInt("simulator-hz", hilSimulatorStateHz)
                    .putString("connection-mode", hilConnectionMode.name())
                    .apply();
            if (aircraftBridge != null) aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
            hilController.start(config);
            if (!hilController.isRunning()) {
                throw new IllegalStateException(getString(R.string.hil_udp_local_port_open_failed, 30021));
            }
            Mini2AircraftBridge.SimulatorSample existingSample = aircraftBridge == null
                    ? null : aircraftBridge.currentSimulatorSample();
            if (existingSample != null) {
                boolean movingSample = existingSample.getMotorsOn() || existingSample.getFlying();
                if (edu.playground.djivln.hil.HilPoseFreshnessPolicy.isSampleUsable(
                        existingSample.getElapsedRealtimeNanos(),
                        SystemClock.elapsedRealtimeNanos(), movingSample)) {
                    submitHilSimulatorSample(existingSample, true);
                    appendLog("HIL new session seeded from existing DJI Simulator RAW with zero command");
                } else {
                    appendLog("HIL existing moving RAW is stale; new session waits for a fresh callback");
                }
            }
            hilStartedSimulator = false;
            hilSimulatorAutoStartPending = true;
            ensureHilSimulatorStarted();
            appendLog(String.format(Locale.US,
                    "HIL link started mode=%s host=%s udp=%d local=30021 frame=%d pose=%dHz",
                    hilConnectionMode.name(), hilConnectionMode == HilConnectionMode.HOTSPOT
                            ? "auto-discovery" : host,
                    udpPort, framePort, hilSimulatorStateHz));
        } catch (Throwable error) {
            showBanner(getString(R.string.hil_start_failed, error.getMessage() == null
                    ? error.getClass().getSimpleName() : error.getMessage()));
            appendLog("HIL start failed: " + error);
        }
        renderHilStatus();
    }

    private void ensureHilSimulatorStarted() {
        if (hilAppInBackground || !hilSimulatorAutoStartPending || hilSimulatorStartInFlight) return;
        if (hilController == null || !hilController.isRunning() || aircraftBridge == null) return;
        if (!aircraftSnapshot.getConnected() || !aircraftSnapshot.getSimulatorAvailable()) return;
        Mini2AircraftBridge.SimulatorSample sample = aircraftBridge.currentSimulatorSample();
        boolean sampleMoving = sample != null && (sample.getMotorsOn() || sample.getFlying());
        boolean sampleFresh = sample != null
                && edu.playground.djivln.hil.HilPoseFreshnessPolicy.isSampleUsable(
                        sample.getElapsedRealtimeNanos(), SystemClock.elapsedRealtimeNanos(),
                        sampleMoving);
        boolean simulatorActuallyActive = aircraftBridge.isSimulatorActuallyActive();
        if (sampleFresh) {
            boolean airborne = sample.getMotorsOn() || sample.getFlying();
            hilSimulatorNavigationUnreadySinceMs = 0L;
            hilSimulatorGroundRecoveryInFlight = false;
            if (airborne) {
                hilSimulatorAutoStartPending = false;
                appendLog("HIL adopting active airborne DJI Simulator; automatic restart forbidden");
                return;
            }
            if (!airborne && hilSimulatorCleanStartRequired) {
                restartGroundedHilSimulatorAfterProcessStart();
                return;
            }
            if (simulatorActuallyActive) {
                hilSimulatorAutoStartPending = false;
                appendLog(hilSimulatorRegressionActive
                    ? "HIL_REGRESSION adopting active DJI Simulator session"
                    : "HIL reusing active DJI Simulator session with fresh RAW state");
                return;
            }
        }
        hilSimulatorNavigationUnreadySinceMs = 0L;
        hilSimulatorStartInFlight = true;
        aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
        if (simulatorActuallyActive) {
            int generation = ++hilSimulatorStartGeneration;
            int attempt = ++hilSimulatorStartAttempts;
            boolean rebound = aircraftBridge.refreshSimulatorStateCallback();
            appendLog("HIL Simulator active; refreshing stale State callback without restart"
                    + " attempt=" + attempt + " rebound=" + rebound);
            mainHandler.postDelayed(() -> {
                if (generation != hilSimulatorStartGeneration) return;
                hilSimulatorStartInFlight = false;
                if (attempt >= 5) {
                    hilSimulatorAutoStartPending = false;
                    appendLog("HIL Simulator kept active but State callback remained stale after "
                            + attempt + " refresh attempts");
                    renderHilStatus();
                    return;
                }
                ensureHilSimulatorStarted();
            }, 500L);
            return;
        }
        startHilOwnedSimulator();
    }

    private void restartGroundedHilSimulatorAfterProcessStart() {
        if (hilSimulatorStartInFlight || aircraftBridge == null) return;
        hilSimulatorStartInFlight = true;
        int generation = ++hilSimulatorStartGeneration;
        appendLog("HIL clean-start resetting grounded Simulator after App process/update");
        stopGroundedHilSimulatorForCleanStart(generation, 1);
    }

    private void stopGroundedHilSimulatorForCleanStart(int generation, int attempt) {
        if (generation != hilSimulatorStartGeneration || aircraftBridge == null) return;
        aircraftBridge.setSimulatorEnabled(false, (stopped, stopMessage) -> {
            if (generation != hilSimulatorStartGeneration) return;
            appendLog("HIL clean-start stop attempt=" + attempt
                    + " ok=" + stopped + " message=" + stopMessage);
            if (!stopped) {
                boolean grounded = !aircraftSnapshot.getSimulatorMotorsOn()
                        && !aircraftSnapshot.getSimulatorFlying();
                if (grounded && attempt < 3) {
                    appendLog("HIL clean-start stop timed out while grounded; retrying after settle");
                    mainHandler.postDelayed(
                            () -> stopGroundedHilSimulatorForCleanStart(generation, attempt + 1),
                            1_500L);
                } else {
                    hilSimulatorStartInFlight = false;
                    hilSimulatorAutoStartPending = false;
                    appendLog("HIL clean-start failed; grounded Simulator could not stop");
                    renderHilStatus();
                }
                return;
            }
            mainHandler.postDelayed(() -> {
                if (generation != hilSimulatorStartGeneration || aircraftBridge == null) return;
                aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
                aircraftBridge.setSimulatorEnabled(true, (started, startMessage) -> {
                    if (generation != hilSimulatorStartGeneration) return;
                    hilSimulatorStartInFlight = false;
                    appendLog("HIL clean-start start ok=" + started + " message=" + startMessage);
                    if (started) {
                        hilStartedSimulator = true;
                        hilSimulatorCleanStartRequired = false;
                        hilSimulatorAutoStartPending = false;
                        hilSimulatorStartAttempts = 0;
                        hilSimulatorCommandPoisoned = false;
                        hilSimulatorCommandPoisonReason = "";
                    } else {
                        hilSimulatorAutoStartPending = false;
                    }
                    renderHilStatus();
                });
            }, 500L);
        });
    }

    private void startHilOwnedSimulator() {
        if (hilController == null || !hilController.isRunning() || aircraftBridge == null) {
            hilSimulatorStartInFlight = false;
            hilSimulatorAutoStartPending = false;
            return;
        }
        int generation = ++hilSimulatorStartGeneration;
        int attempt = ++hilSimulatorStartAttempts;
        appendLog("HIL auto-starting DJI Simulator at " + hilSimulatorStateHz
                + " Hz attempt=" + attempt);
        mainHandler.postDelayed(() -> {
            if (!hilSimulatorStartInFlight || generation != hilSimulatorStartGeneration) return;
            hilSimulatorStartInFlight = false;
            if (aircraftBridge.isSimulatorActuallyActive()) {
                hilStartedSimulator = true;
                aircraftBridge.refreshSimulatorStateCallback();
                hilSimulatorCleanStartRequired = false;
                hilSimulatorAutoStartPending = false;
                hilSimulatorStartAttempts = 0;
                appendLog("HIL Simulator start callback timeout, but fresh SDK session is active; adopting session");
            } else if (hilController != null && hilController.isRunning() && attempt < 3) {
                hilSimulatorAutoStartPending = true;
                appendLog("HIL Simulator start timeout attempt=" + attempt
                        + " · retrying without stop/start reset");
                mainHandler.postDelayed(this::ensureHilSimulatorStarted, 500L);
            } else {
                hilSimulatorAutoStartPending = false;
                appendLog("HIL Simulator auto-start exhausted after " + attempt + " attempts");
            }
        }, 6_000L);
        aircraftBridge.setSimulatorEnabled(true, (ok, message) -> {
            if (generation != hilSimulatorStartGeneration) {
                appendLog("HIL Simulator stale start callback generation=" + generation
                        + " ok=" + ok + " · " + message);
                if (ok && aircraftBridge != null && aircraftBridge.isSimulatorActuallyActive()) {
                    hilStartedSimulator = true;
                    appendLog("HIL preserving Simulator activated by stale callback");
                }
                return;
            }
            hilSimulatorStartInFlight = false;
            boolean linkStillRunning = hilController != null && hilController.isRunning();
            if (ok && linkStillRunning) {
                hilStartedSimulator = true;
                hilSimulatorStartAttempts = 0;
                hilSimulatorCleanStartRequired = false;
                hilSimulatorAutoStartPending = false;
                appendLog("HIL Simulator fresh auto-start OK · " + message);
            } else if (ok) {
                hilStartedSimulator = true;
                appendLog("HIL closed during Simulator start; preserving active Simulator session");
            } else if (linkStillRunning && attempt < 3) {
                hilSimulatorAutoStartPending = true;
                appendLog("HIL Simulator auto-start FAIL · " + message
                        + " · retrying attempt=" + (attempt + 1));
                mainHandler.postDelayed(this::ensureHilSimulatorStarted, 750L);
            } else {
                hilSimulatorAutoStartPending = false;
                appendLog("HIL Simulator auto-start exhausted after " + attempt
                        + " attempts · " + message);
            }
            renderHilStatus();
        });
    }

    private void stopHilLink(String reason, boolean stopOwnedSimulator) {
        boolean stopSimulator = stopOwnedSimulator && hilStartedSimulator;
        hilSimulatorStartGeneration += 1;
        hilSimulatorStartAttempts = 0;
        hilSimulatorAutoStartPending = false;
        hilStartedSimulator = false;
        if (hilController != null) hilController.stop();
        if (stopSimulator && aircraftBridge != null && aircraftSnapshot.getSimulatorActive()) {
            appendLog("HIL stopping owned DJI Simulator · " + reason);
            aircraftBridge.setSimulatorEnabled(false);
        } else if (aircraftSnapshot.getSimulatorActive()) {
            appendLog("HIL link stopped; DJI Simulator session kept active · " + reason);
        }
    }

    private int parseHilPort(int viewId, int fallback) {
        String text = ((EditText) findViewById(viewId)).getText().toString().trim();
        int value = text.isEmpty() ? fallback : Integer.parseInt(text);
        if (value < 1 || value > 65_535) {
            throw new IllegalArgumentException(getString(R.string.port_out_of_range));
        }
        return value;
    }

    private void startHilUiSimulatorTakeoff() {
        if (hilSimulatorCommandPoisoned) {
            String reason = hilSimulatorCommandPoisonReason.isEmpty()
                    ? getString(R.string.dji_simulator_command_state_invalid)
                    : hilSimulatorCommandPoisonReason;
            appendLog("HIL UI TAKEOFF blocked by command fuse · " + reason);
            showBanner(getString(R.string.sim_takeoff_fused, reason));
            return;
        }
        if (hilUiSimulatorTakeoffPending) {
            showBanner(getString(R.string.sim_takeoff_waiting_controller));
            return;
        }
        hilUiSimulatorTakeoffPending = true;
        int generation = ++hilUiSimulatorTakeoffGeneration;
        hilUiSimulatorTakeoffStartedMs = SystemClock.elapsedRealtime();
        hilUiSimulatorTakeoffReadySinceMs = 0L;
        hilSimulatorNavigationUnreadySinceMs = 0L;
        hilUiSimulatorTakeoffAttempts = 0;
        hilUiSimulatorTakeoffAccepted = false;
        hilUiSimulatorPostAcceptRefreshAttempted = false;
        hilUiSimulatorAirborneDeadlineMs = 0L;
        hilUiSimulatorRecoveryInFlight = false;
        hilUiSimulatorRecoveryAttempted = false;
        hilUiSimulatorProductReconnectAttempted = false;
        hilUiSimulatorMotorFallbackActive = false;
        hilUiSimulatorMotorFallbackDeadlineMs = 0L;
        appendLog("HIL UI TAKEOFF requested; waiting for fresh RAW SimulatorState");
        showBanner(getString(R.string.sim_waiting_controller));
        mainHandler.post(() -> pollHilUiSimulatorTakeoff(generation));
    }

    private void pollHilUiSimulatorTakeoff(int generation) {
        if (!hilUiSimulatorTakeoffPending || generation != hilUiSimulatorTakeoffGeneration) return;
        long now = SystemClock.elapsedRealtime();
        if (aircraftBridge == null) {
            finishHilUiSimulatorTakeoff(false, getString(R.string.flight_controller_bridge_unavailable));
            return;
        }
        if (hilUiSimulatorRecoveryInFlight) return;
        if (hilSimulatorStartInFlight || hilSimulatorCleanStartRequired) {
            hilUiSimulatorTakeoffReadySinceMs = 0L;
            if (now - hilUiSimulatorTakeoffStartedMs >= 30_000L) {
                finishHilUiSimulatorTakeoff(false,
                        getString(R.string.wait_simulator_initialization_timeout,
                                aircraftBridge.simulatorRegressionDiagnostics()));
                return;
            }
            mainHandler.postDelayed(() -> pollHilUiSimulatorTakeoff(generation), 100L);
            return;
        }
        boolean rawStateReady = aircraftBridge.isSimulatorRegressionReady();
        boolean navigationReady = aircraftBridge.isSimulatorNavigationReady();
        if (!rawStateReady
                && !hilUiSimulatorRecoveryAttempted
                && now - hilUiSimulatorTakeoffStartedMs >= 1_000L
                && aircraftSnapshot.getSimulatorActive()
                && !aircraftSnapshot.getSimulatorMotorsOn()
                && !aircraftSnapshot.getSimulatorFlying()) {
            recoverGroundedHilSimulatorForTakeoff(
                    generation,
                    "stale RAW state",
                    getString(R.string.simulator_state_stale_auto_recovering));
            return;
        }
        if (now - hilUiSimulatorTakeoffStartedMs >= 30_000L) {
            finishHilUiSimulatorTakeoff(false,
                    getString(R.string.wait_simulator_flight_controller_timeout,
                            aircraftBridge.simulatorRegressionDiagnostics()));
            return;
        }
        if (rawStateReady && navigationReady) {
            hilSimulatorNavigationUnreadySinceMs = 0L;
            if (hilUiSimulatorTakeoffReadySinceMs == 0L) {
                hilUiSimulatorTakeoffReadySinceMs = now;
                appendLog("HIL UI TAKEOFF RAW + navigation ready; settling for 1000ms");
            }
        } else {
            hilUiSimulatorTakeoffReadySinceMs = 0L;
            if (rawStateReady && hilSimulatorNavigationUnreadySinceMs == 0L) {
                hilSimulatorNavigationUnreadySinceMs = now;
                appendLog("HIL UI TAKEOFF RAW ready; waiting for P-GPS/Home/location");
            }
        }
        if (hilUiSimulatorTakeoffReadySinceMs == 0L
                || now - hilUiSimulatorTakeoffReadySinceMs < 1_000L) {
            mainHandler.postDelayed(() -> pollHilUiSimulatorTakeoff(generation), 100L);
            return;
        }
        if (aircraftSnapshot.getVirtualStickEnabled()) {
            aircraftBridge.disableSimulatorRegressionVirtualStick((ok, message) -> {
                if (!hilUiSimulatorTakeoffPending
                        || generation != hilUiSimulatorTakeoffGeneration) return;
                appendLog("HIL UI TAKEOFF preflight VS release ok=" + ok + " message=" + message);
                if (!ok) {
                    finishHilUiSimulatorTakeoff(false,
                            getString(R.string.virtual_stick_release_failed, message));
                    return;
                }
                mainHandler.postDelayed(() -> requestHilUiSimulatorTakeoff(generation), 1_500L);
            });
            return;
        }
        requestHilUiSimulatorTakeoff(generation);
    }

    private void recoverGroundedHilSimulatorForTakeoff(
            int generation,
            String reason,
            String banner) {
        if (!hilUiSimulatorTakeoffPending
                || generation != hilUiSimulatorTakeoffGeneration
                || aircraftBridge == null) return;
        hilUiSimulatorRecoveryAttempted = true;
        hilUiSimulatorRecoveryInFlight = true;
        appendLog("HIL UI TAKEOFF restarting grounded Simulator session · " + reason);
        showBanner(banner);
        aircraftBridge.setSimulatorEnabled(false, (stopped, stopMessage) -> {
            if (!hilUiSimulatorTakeoffPending
                    || generation != hilUiSimulatorTakeoffGeneration) return;
            appendLog("HIL UI TAKEOFF recovery stop ok=" + stopped + " message=" + stopMessage);
            if (!stopped) {
                hilUiSimulatorRecoveryInFlight = false;
                finishHilUiSimulatorTakeoff(false,
                        getString(R.string.auto_reset_simulator_failed, stopMessage));
                return;
            }
            mainHandler.postDelayed(() -> {
                if (!hilUiSimulatorTakeoffPending
                        || generation != hilUiSimulatorTakeoffGeneration
                        || aircraftBridge == null) return;
                aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
                aircraftBridge.setSimulatorEnabled(true, (started, startMessage) -> {
                    if (!hilUiSimulatorTakeoffPending
                            || generation != hilUiSimulatorTakeoffGeneration) return;
                    appendLog("HIL UI TAKEOFF recovery start ok=" + started
                            + " message=" + startMessage);
                    hilUiSimulatorRecoveryInFlight = false;
                    if (!started) {
                        finishHilUiSimulatorTakeoff(false,
                                getString(R.string.auto_restart_simulator_failed, startMessage));
                        return;
                    }
                    hilStartedSimulator = true;
                    hilSimulatorCleanStartRequired = false;
                    hilSimulatorCommandPoisoned = false;
                    hilSimulatorCommandPoisonReason = "";
                    hilUiSimulatorTakeoffAttempts = 0;
                    hilUiSimulatorTakeoffStartedMs = SystemClock.elapsedRealtime();
                    hilUiSimulatorTakeoffReadySinceMs = 0L;
                    mainHandler.postDelayed(() -> pollHilUiSimulatorTakeoff(generation), 250L);
                });
            }, 500L);
        });
    }

    private void requestHilUiSimulatorTakeoff(int generation) {
        if (!hilUiSimulatorTakeoffPending || generation != hilUiSimulatorTakeoffGeneration) return;
        long flightStateAgeMs = aircraftSnapshot.getFlightStateUpdatedAtMs() <= 0L
                ? Long.MAX_VALUE
                : Math.max(0L, System.currentTimeMillis()
                        - aircraftSnapshot.getFlightStateUpdatedAtMs());
        if (flightStateAgeMs > 1_500L) {
            finishHilUiSimulatorTakeoff(false,
                    getString(R.string.flight_state_stale_takeoff_not_sent, flightStateAgeMs));
            return;
        }
        int attempt = ++hilUiSimulatorTakeoffAttempts;
        appendLog("HIL UI TAKEOFF command attempt=" + attempt + " "
                + aircraftBridge.simulatorRegressionDiagnostics());
        aircraftBridge.requestSimulatorRegressionTakeoff((ok, message) -> {
            if (!hilUiSimulatorTakeoffPending
                    || generation != hilUiSimulatorTakeoffGeneration) return;
            appendLog("HIL UI TAKEOFF result attempt=" + attempt
                    + " ok=" + ok + " message=" + message);
            if (ok) {
                hilUiSimulatorTakeoffAccepted = true;
                hilUiSimulatorAirborneDeadlineMs = SystemClock.elapsedRealtime() + 15_000L;
                appendLog("HIL UI TAKEOFF command accepted; waiting for observed airborne state");
        showBanner(getString(R.string.takeoff_command_accepted_confirming));
                mainHandler.post(() -> pollHilUiSimulatorAirborne(generation));
            } else if (isHilSimulatorCommandPoison(message)) {
                boolean grounded = !aircraftSnapshot.getSimulatorMotorsOn()
                        && !aircraftSnapshot.getSimulatorFlying();
                if (grounded && !hilUiSimulatorMotorFallbackActive) {
                    startHilUiSimulatorMotorFallback(generation, message);
                    return;
                }
                if (!hilUiSimulatorProductReconnectAttempted && grounded) {
                    reconnectHilProductForTakeoff(generation, message);
                    return;
                }
                tripHilSimulatorCommandFuse(message);
                finishHilUiSimulatorTakeoff(false,
                        getString(R.string.dji_unrecoverable_after_rebuild));
            } else if (attempt < 3) {
                mainHandler.postDelayed(() -> requestHilUiSimulatorTakeoff(generation), 1_000L);
            } else {
                finishHilUiSimulatorTakeoff(false,
                        getString(R.string.dji_rejected_consecutive_attempts, attempt, message));
            }
        });
    }

    private void pollHilUiSimulatorAirborne(int generation) {
        if (!hilUiSimulatorTakeoffPending
                || !hilUiSimulatorTakeoffAccepted
                || generation != hilUiSimulatorTakeoffGeneration) return;
        if (aircraftSnapshot.getSimulatorFlying()
                && aircraftSnapshot.getAltitude() >= 0.8
                && aircraftBridge != null
                && aircraftBridge.isSimulatorTakeoffTransitionComplete()) {
            hilSimulatorCommandPoisoned = false;
            hilSimulatorCommandPoisonReason = "";
            finishHilUiSimulatorTakeoff(true, getString(R.string.simulator_airborne_stable_observed));
            return;
        }
        if (SystemClock.elapsedRealtime() < hilUiSimulatorAirborneDeadlineMs) {
            mainHandler.postDelayed(() -> pollHilUiSimulatorAirborne(generation), 100L);
            return;
        }
        if (!hilUiSimulatorPostAcceptRefreshAttempted && aircraftBridge != null) {
            hilUiSimulatorPostAcceptRefreshAttempted = true;
            boolean refreshed = aircraftBridge.refreshSimulatorStateCallback();
            appendLog("HIL UI TAKEOFF accepted but not airborne; final RAW refresh=" + refreshed);
            mainHandler.postDelayed(() -> {
                if (!hilUiSimulatorTakeoffPending
                        || generation != hilUiSimulatorTakeoffGeneration) return;
                if (aircraftSnapshot.getSimulatorFlying()
                        && aircraftSnapshot.getAltitude() >= 0.8
                        && aircraftBridge != null
                        && aircraftBridge.isSimulatorTakeoffTransitionComplete()) {
                    finishHilUiSimulatorTakeoff(true,
                            getString(R.string.simulator_airborne_confirmed_after_final_refresh));
                } else {
                    finishHilUiSimulatorTakeoff(false,
                            getString(R.string.takeoff_accepted_no_airborne_no_motor_fallback));
                }
            }, 500L);
            return;
        }
        finishHilUiSimulatorTakeoff(false,
                getString(R.string.takeoff_accepted_no_airborne_no_motor_fallback));
    }

    private void startHilUiSimulatorMotorFallback(int generation, String takeoffError) {
        if (!hilUiSimulatorTakeoffPending
                || generation != hilUiSimulatorTakeoffGeneration
                || aircraftBridge == null) return;
        hilUiSimulatorMotorFallbackActive = true;
        hilUiSimulatorRecoveryInFlight = true;
        appendLog("HIL UI TAKEOFF native 255; using motor + temporary VS fallback · "
                + takeoffError);
        showBanner(getString(R.string.native_takeoff_unavailable_sim_path));
        aircraftBridge.requestSimulatorRegressionMotorsOn((motorsOk, motorsMessage) -> {
            if (!hilUiSimulatorTakeoffPending
                    || generation != hilUiSimulatorTakeoffGeneration) return;
            appendLog("HIL UI TAKEOFF motor fallback motors ok=" + motorsOk
                    + " message=" + motorsMessage);
            if (!motorsOk) {
                hilUiSimulatorMotorFallbackActive = false;
                hilUiSimulatorRecoveryInFlight = false;
                if (!hilUiSimulatorProductReconnectAttempted) {
                    reconnectHilProductForTakeoff(generation,
                            takeoffError + "; motors=" + motorsMessage);
                } else {
                    tripHilSimulatorCommandFuse(takeoffError + "; motors=" + motorsMessage);
                    finishHilUiSimulatorTakeoff(false,
                            getString(R.string.dji_takeoff_and_motor_start_failed, motorsMessage));
                }
                return;
            }
            aircraftBridge.enableSimulatorRegressionVirtualStick((vsOk, vsMessage) -> {
                if (!hilUiSimulatorTakeoffPending
                        || generation != hilUiSimulatorTakeoffGeneration) return;
                appendLog("HIL UI TAKEOFF motor fallback VS ok=" + vsOk
                        + " message=" + vsMessage);
                if (!vsOk) {
                    hilUiSimulatorMotorFallbackActive = false;
                    hilUiSimulatorRecoveryInFlight = false;
                    finishHilUiSimulatorTakeoff(false,
                            getString(R.string.motors_started_temp_vs_failed, vsMessage));
                    return;
                }
                hilUiSimulatorRecoveryInFlight = false;
                hilUiSimulatorMotorFallbackDeadlineMs = SystemClock.elapsedRealtime() + 8_000L;
                mainHandler.post(() -> driveHilUiSimulatorMotorFallback(generation));
            });
        });
    }

    private void driveHilUiSimulatorMotorFallback(int generation) {
        if (!hilUiSimulatorTakeoffPending
                || generation != hilUiSimulatorTakeoffGeneration
                || !hilUiSimulatorMotorFallbackActive
                || aircraftBridge == null) return;
        boolean airborne = aircraftSnapshot.getSimulatorFlying()
                && aircraftSnapshot.getAltitude() >= 0.8;
        if (airborne) {
            aircraftBridge.sendSimulatorRegressionSticks(0, 0, 0, 0);
            hilUiSimulatorMotorFallbackActive = false;
            aircraftBridge.disableSimulatorRegressionVirtualStick((released, releaseMessage) -> {
                if (!hilUiSimulatorTakeoffPending
                        || generation != hilUiSimulatorTakeoffGeneration) return;
                appendLog("HIL UI TAKEOFF motor fallback airborne; VS release ok=" + released
                        + " message=" + releaseMessage);
                hilSimulatorCommandPoisoned = false;
                hilSimulatorCommandPoisonReason = "";
                finishHilUiSimulatorTakeoff(released, released
                        ? getString(R.string.simulator_motor_takeoff_success_rc_restored)
                        : getString(R.string.airborne_vs_release_failed, releaseMessage));
            });
            return;
        }
        if (SystemClock.elapsedRealtime() >= hilUiSimulatorMotorFallbackDeadlineMs) {
            aircraftBridge.sendSimulatorRegressionSticks(0, 0, 0, 0);
            hilUiSimulatorMotorFallbackActive = false;
            aircraftBridge.disableSimulatorRegressionVirtualStick((released, releaseMessage) ->
                    finishHilUiSimulatorTakeoff(false,
                            getString(R.string.motor_path_takeoff_unconfirmed,
                                    released, releaseMessage)));
            return;
        }
        if (!aircraftBridge.sendSimulatorRegressionSticks(0, 330, 0, 0)) {
            hilUiSimulatorMotorFallbackActive = false;
            finishHilUiSimulatorTakeoff(false, getString(R.string.motor_path_ascent_blocked));
            return;
        }
        mainHandler.postDelayed(() -> driveHilUiSimulatorMotorFallback(generation), 40L);
    }

    private void reconnectHilProductForTakeoff(int generation, String reason) {
        if (!hilUiSimulatorTakeoffPending
                || generation != hilUiSimulatorTakeoffGeneration) return;
        hilUiSimulatorProductReconnectAttempted = true;
        hilUiSimulatorRecoveryInFlight = true;
        hilSimulatorStartGeneration += 1;
        hilSimulatorStartInFlight = false;
        hilSimulatorAutoStartPending = true;
        hilSimulatorCleanStartRequired = false;
        appendLog("HIL UI TAKEOFF reconnecting DJI product session after command poison · " + reason);
        showBanner(getString(R.string.sim_command_reconnecting));
        DJISDKManager.getInstance().stopConnectionToProduct();
        mainHandler.postDelayed(() -> {
            if (!hilUiSimulatorTakeoffPending
                    || generation != hilUiSimulatorTakeoffGeneration) return;
            boolean requested = DJISDKManager.getInstance().startConnectionToProduct();
            appendLog("HIL UI TAKEOFF product reconnect requested=" + requested);
            hilUiSimulatorRecoveryInFlight = false;
            hilUiSimulatorRecoveryAttempted = false;
            hilUiSimulatorTakeoffAttempts = 0;
            hilUiSimulatorTakeoffStartedMs = SystemClock.elapsedRealtime();
            hilUiSimulatorTakeoffReadySinceMs = 0L;
            hilSimulatorCommandPoisoned = false;
            hilSimulatorCommandPoisonReason = "";
            mainHandler.postDelayed(() -> pollHilUiSimulatorTakeoff(generation), 500L);
        }, 1_500L);
    }

    private void finishHilUiSimulatorTakeoff(boolean ok, String message) {
        hilUiSimulatorTakeoffPending = false;
        hilUiSimulatorRecoveryInFlight = false;
        hilUiSimulatorMotorFallbackActive = false;
        hilUiSimulatorMotorFallbackDeadlineMs = 0L;
        hilUiSimulatorTakeoffAccepted = false;
        hilUiSimulatorPostAcceptRefreshAttempted = false;
        hilUiSimulatorAirborneDeadlineMs = 0L;
        hilUiSimulatorTakeoffGeneration += 1;
        appendLog("HIL UI TAKEOFF " + (ok ? "PASS" : "FAIL") + " · " + message);
        showBanner(ok ? getString(R.string.sim_takeoff_accepted)
                : getString(R.string.sim_takeoff_failed, message));
    }

    private boolean isHilSimulatorCommandPoison(String message) {
        if (message == null) return false;
        return message.contains("code=255") || message.contains("Undefined Error");
    }

    private void tripHilSimulatorCommandFuse(String message) {
        hilSimulatorCommandPoisoned = true;
        hilSimulatorCommandPoisonReason = message == null ? "Undefined Error code=255" : message;
        appendLog("HIL Simulator command fuse TRIPPED · " + hilSimulatorCommandPoisonReason);
    }

    private void toggleHilVirtualFrames() {
        if (hilController == null || !hilController.isRunning()) {
            showBanner(getString(R.string.hil_start_first));
            return;
        }
        if (hasActiveHilControlledOperation()) normalStop(getString(R.string.reason_switch_dji_ue_image_source));
        hilVirtualFramesEnabled = !hilVirtualFramesEnabled;
        if (hilVirtualFramesEnabled) {
            hilVirtualFramesEnabledAtElapsedMs = SystemClock.elapsedRealtime();
            hilVirtualFrameUnavailableSinceElapsedMs = 0L;
            hilVirtualFrameEverReady = false;
            hilVirtualFrameWaitLogged = false;
        } else {
            resetHilVirtualFrameAvailabilityTracking();
        }
        appendLog("HIL virtual camera=" + hilVirtualFramesEnabled);
        if (hilVirtualFramesEnabled) {
            AndroidHilController.Status status = hilController.latestStatus();
            if (status == null || !status.getFrameListening()) {
                showBanner(getString(R.string.hil_ue_tcp_not_ready));
            } else if (!status.getFrameConnected()) {
                showBanner(getString(R.string.hil_ue_connecting_frames));
            } else if (hilController.frameSnapshot() == null) {
                showBanner(getString(R.string.hil_ue_waiting_first_frame));
            } else {
            showBanner(getString(R.string.hil_inference_source_ue));
            }
        } else {
            showBanner(getString(R.string.hil_inference_source_dji));
        }
        renderHilStatus();
        renderSafetyState();
    }

    private void monitorHilVirtualFrameAvailability(AndroidHilController.Status status) {
        if (!hilVirtualFramesEnabled || !status.getRunning()) {
            if (!hilVirtualFramesEnabled) resetHilVirtualFrameAvailabilityTracking();
            return;
        }
        long now = SystemClock.elapsedRealtime();
        edu.playground.djivln.hil.HilVirtualFrameStore.Snapshot frame = status.getFrame();
        boolean freshFrame = frame != null && frame.getAgeMillis() <= HIL_ASYNC_FRAME_FRESH_MILLIS;
        if (freshFrame) {
            if (!hilVirtualFrameEverReady) {
                appendLog("HIL virtual camera first frame ready id=" + frame.getFrameId()
                        + " age=" + frame.getAgeMillis() + "ms");
            }
            hilVirtualFrameEverReady = true;
            hilVirtualFrameUnavailableSinceElapsedMs = 0L;
            hilVirtualFrameWaitLogged = false;
            return;
        }
        if (hilVirtualFrameUnavailableSinceElapsedMs == 0L) {
            hilVirtualFrameUnavailableSinceElapsedMs = now;
        }
        long unavailableMs = now - hilVirtualFrameUnavailableSinceElapsedMs;
        long graceMs = hilVirtualFrameEverReady
                ? HIL_ASYNC_FRAME_DROPOUT_GRACE_MILLIS
                : HIL_ASYNC_FRAME_STARTUP_GRACE_MILLIS;
        if (!hilVirtualFrameWaitLogged) {
            hilVirtualFrameWaitLogged = true;
            appendLog("HIL virtual camera waiting async frame · grace=" + graceMs
                    + "ms connected=" + status.getFrameConnected());
        }
        if (!hasActiveHilControlledOperation() || unavailableMs < graceMs) return;
        String reason = getString(hilVirtualFrameEverReady
                        ? R.string.ue_virtual_camera_frame_dropout
                        : R.string.ue_virtual_camera_first_frame_timeout,
                graceMs / 1_000L);
        if (isSurveySimulatorExecutionActive()) {
            pauseSurveyForRecoverableFault(reason, true);
        } else {
            normalStop(reason);
        }
    }

    private void resetHilVirtualFrameAvailabilityTracking() {
        hilVirtualFramesEnabledAtElapsedMs = 0L;
        hilVirtualFrameUnavailableSinceElapsedMs = 0L;
        hilVirtualFrameEverReady = false;
        hilVirtualFrameWaitLogged = false;
    }

    private void previewLatestHilFrame() {
        AndroidHilController controller = hilController;
        edu.playground.djivln.hil.HilVirtualFrameStore.Snapshot snapshot =
                controller == null ? null : controller.frameSnapshot();
        if (controller == null || !controller.isRunning() || snapshot == null) {
            showBanner(getString(R.string.hil_ue_frame_not_ready));
            return;
        }
        modelExecutor.execute(() -> {
            Bitmap bitmap = controller.decodeLatestFrame(5_000L);
            if (bitmap == null) {
                runOnUiThread(() -> showBanner(getString(R.string.hil_ue_frame_decode_failed)));
                return;
            }
            runOnUiThread(() -> showHilFramePreviewDialog(bitmap, snapshot));
        });
    }

    private void showHilFramePreviewDialog(
            Bitmap bitmap,
            edu.playground.djivln.hil.HilVirtualFrameStore.Snapshot snapshot) {
        if (isFinishing() || isDestroyed()) {
            bitmap.recycle();
            return;
        }
        ImageView preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setBackgroundColor(Color.BLACK);
        preview.setPadding(dp(8), dp(8), dp(8), dp(8));
        preview.setImageBitmap(bitmap);
        String title = getString(R.string.ue_current_frame_summary,
                snapshot.getFrameId(), snapshot.getWidth(), snapshot.getHeight(),
                snapshot.getAgeMillis(), snapshot.getMeasuredReceiveHz());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(preview)
                .setNegativeButton(R.string.action_close, null)
                .setPositiveButton(R.string.action_refresh, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    previewLatestHilFrame();
                }));
        dialog.setOnDismissListener(ignored -> {
            preview.setImageDrawable(null);
            if (!bitmap.isRecycled()) bitmap.recycle();
        });
        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(48), dp(960));
        dialog.getWindow().setAttributes(params);
    }

    private void handleHilEvent(HilProtocol.Event event) {
        String reason = event.getReason().isEmpty() ? getString(R.string.ue_reason_not_provided) : event.getReason();
        appendLog(String.format(Locale.US, "HIL event kind=%d stop=%.3f pose=%d · %s",
                event.getKind(), event.getStopScore(), event.getPoseSequence(), reason));
        if (event.getKind() == HilProtocol.EVENT_EMERGENCY) {
            activateEmergencyStop(getString(R.string.ue_hil_emergency_stop, reason));
        } else if (event.getKind() == HilProtocol.EVENT_COLLISION) {
            if (hasActiveHilControlledOperation()) normalStop(getString(R.string.ue_collision, reason));
        } else if (event.getKind() == HilProtocol.EVENT_STOP
                && Double.isFinite(event.getStopScore()) && event.getStopScore() >= stopThreshold) {
            if (hasActiveHilControlledOperation()) normalStop(String.format(Locale.US,
                    "UE STOP %.2f ≥ %.1f：%s", event.getStopScore(), stopThreshold, reason));
        }
    }

    private boolean hasActiveHilControlledOperation() {
        return controlArmed || autoInferenceEnabled || inferenceInFlight || chunkExecutionActive
                || isSurveySimulatorExecutionActive();
    }

    /**
     * A DJI source/lifecycle loss is a control fault, not a HIL network stop.
     * Keep UDP/TCP bound so UE can reconnect, but never let the last moving pose
     * continue to look authoritative and never resume control automatically.
     */
    private void pauseHilForTransientSourceLoss(String reason) {
        AndroidHilController controller = hilController;
        if (controller == null || !controller.isRunning()) return;
        controller.clearPose();
        Mini2AircraftBridge.SimulatorSample sample = aircraftBridge == null
                ? null : aircraftBridge.currentSimulatorSample();
        boolean moving = sample != null && (sample.getMotorsOn() || sample.getFlying());
        hilMovingRawStaleLatched = moving;
        if (isSurveySimulatorExecutionActive()) {
            pauseSurveyForRecoverableFault(reason, true);
        } else if (hasActiveHilControlledOperation()) {
            normalStop(reason);
        }
        appendLog(getString(R.string.hil_source_loss_pose_cleared, reason));
        if (moving) showBanner(getString(R.string.control_paused_manual_resume, reason));
    }

    private void invalidateHilSimulatorActivation(String reason) {
        if (!hilSimulatorStartInFlight) return;
        hilSimulatorStartGeneration += 1;
        hilSimulatorStartInFlight = false;
        appendLog("HIL invalidated pending Simulator activation · " + reason);
    }

    private void renderHilStatus() {
        Button link = findViewById(R.id.hil_link_toggle);
        Button frame = findViewById(R.id.hil_frame_toggle);
        Button preview = findViewById(R.id.hil_frame_preview);
        TextView statusView = findViewById(R.id.hil_status);
        boolean running = hilController != null && hilController.isRunning();
        link.setText(running ? getString(R.string.hil_link_on) : getString(R.string.hil_link_off));
        frame.setEnabled(running);
        frame.setAlpha(running ? 1.0f : 0.35f);
        AndroidHilController.Status status = latestHilStatus;
        boolean ueFrameReady = status != null && status.getFrameConnected() && status.getFrame() != null;
        preview.setEnabled(running && ueFrameReady);
        preview.setAlpha(running && ueFrameReady ? 1.0f : 0.35f);
        frame.setText(hilVirtualFramesEnabled
                ? (ueFrameReady ? R.string.image_source_ue : R.string.image_source_ue_waiting)
                : R.string.image_source_dji);
        styleButton(link,
                running ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                running ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
        styleButton(frame,
                hilVirtualFramesEnabled ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                hilVirtualFramesEnabled ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
        styleButton(preview, getColor(R.color.btn_neutral_fill), getColor(R.color.btn_neutral_stroke), 10);
        if (!running || status == null) {
            statusView.setText(hilConnectionMode == HilConnectionMode.HOTSPOT
                    ? R.string.hil_hotspot_waiting_discovery : R.string.hil_lan_waiting_configured_ue);
            statusView.setTextColor(getColor(R.color.text_accent_amber));
            return;
        }
        String rtt = Double.isFinite(status.getRoundTripMillis())
                ? String.format(Locale.US, "%.1fms", status.getRoundTripMillis()) : "--";
        String frameText = status.getFrame() == null ? getString(R.string.no_frame) : String.format(Locale.US,
                "F%d %dx%d %.1fHz age=%dms", status.getFrame().getFrameId(),
                status.getFrame().getWidth(), status.getFrame().getHeight(),
                status.getFrame().getMeasuredReceiveHz(), status.getFrame().getAgeMillis());
        String peer = status.getPeerHost() == null || status.getPeerHost().isEmpty()
                ? getString(status.getMode() == HilConnectionMode.HOTSPOT
                        ? R.string.broadcast_discovery_in_progress : R.string.status_waiting)
                : status.getPeerHost();
        String tcp = !status.getFrameListening() ? getString(R.string.not_listening)
                : status.getFrameConnected()
                ? getString(R.string.connected_to_peer,
                        status.getFramePeerHost() == null ? "UE" : status.getFramePeerHost())
                : status.getFrame() != null && status.getFrame().getAgeMillis() <= 5_000L
                ? getString(R.string.async_frame_age, status.getFrame().getAgeMillis())
                : getString(R.string.bidirectional_waiting_port,
                        ((EditText) findViewById(R.id.hil_frame_port)).getText());
        statusView.setText(getString(R.string.hil_status_summary,
                getString(status.getMode() == HilConnectionMode.HOTSPOT
                        ? R.string.phone_hotspot : R.string.local_area_network),
                peer, status.getPeerFresh() ? "OK" : getString(R.string.status_waiting),
                status.getMeasuredPoseSendHz(),
                status.getSentPoseCount(), rtt, tcp,
                frameText, aircraftSnapshot.getSimulatorStateHz(), hilSimulatorStateHz,
                aircraftSnapshot.getSimulatorMotorsOn() ? "ON" : "OFF",
                aircraftSnapshot.getSimulatorFlying() ? "YES" : "NO"));
        statusView.setTextColor(getColor(status.getPeerFresh()
                && (!hilVirtualFramesEnabled || ueFrameReady)
                ? R.color.text_ok_green : R.color.text_accent_amber));
    }

    private String ueBridgeEndpoint() {
        String endpoint = ((EditText) findViewById(R.id.ue_bridge_endpoint)).getText().toString().trim();
        getSharedPreferences("ue-bridge", MODE_PRIVATE).edit().putString("endpoint", endpoint).apply();
        return endpoint;
    }

    private String resolvedSurveyUeEndpoint() {
        String configured = ueBridgeEndpoint();
        AndroidHilController.Status status = latestHilStatus;
        String peerHost = status == null ? null : status.getPeerHost();
        if (peerHost == null || peerHost.trim().isEmpty()) return configured;
        Uri parsed = Uri.parse(configured);
        String scheme = parsed.getScheme() == null ? "http" : parsed.getScheme();
        int port = parsed.getPort() > 0 ? parsed.getPort() : 30_010;
        return scheme + "://" + peerHost.trim() + ":" + port;
    }

    private void sendSurveyMissionToUe() {
        TextView status = findViewById(R.id.ue_bridge_status);
        if (surveyMission == null) {
            status.setText(R.string.hil_send_no_mission);
            status.setTextColor(getColor(R.color.text_danger));
            return;
        }
        status.setText(R.string.hil_sending_mission);
        ueBridgeClient.postMission(resolvedSurveyUeEndpoint(), surveyMission, (ok, message) ->
                runOnUiThread(() -> {
                    status.setText(getString(ok
                            ? R.string.hil_mission_sent : R.string.hil_mission_send_failed, message));
                    status.setTextColor(getColor(ok ? R.color.text_ok_green : R.color.text_danger));
                    appendLog("UE bridge mission " + (ok ? "OK " : "FAIL ") + message);
                }));
    }

    private void publishSurveyTargetToUe() {
        if ((!hilVirtualFramesEnabled && !ueBridgeEnabled)
                || surveyMission == null || surveySimulatorExecution == null) return;
        SurveyExecutionStatus status = surveySimulatorExecution.getStatus();
        if (status.getState() != SurveyExecutionState.RUNNING
                && status.getState() != SurveyExecutionState.PAUSED) return;
        SurveyUeTarget target = new SurveyUeTarget(
                System.currentTimeMillis(), surveyMission.getId(), status.getState(),
                surveySimulatorExecution.getCurrentPhase(),
                surveySimulatorExecution.getExecutionLegIndex(), status.getWaypointIndex(),
                surveySimulatorExecution.getCurrentTarget());
        ueBridgeClient.postTarget(resolvedSurveyUeEndpoint(), target, (ok, message) -> {
            if (!ok) runOnUiThread(() -> appendLog("UE bridge target FAIL " + message));
        });
    }

    private void toggleUeBridge() {
        ueBridgeEnabled = !ueBridgeEnabled;
        ((Button) findViewById(R.id.ue_bridge_toggle)).setText(
                ueBridgeEnabled ? R.string.telemetry_mirror_on : R.string.telemetry_mirror_off);
        TextView status = findViewById(R.id.ue_bridge_status);
        status.setText(ueBridgeEnabled
                ? R.string.telemetry_mirror_enabled : R.string.telemetry_contract_summary);
        lastUeBridgePostElapsedMs = 0L;
        appendLog("UE bridge telemetry=" + ueBridgeEnabled + " · outbound-only");
    }

    private void publishUeTelemetryIfEnabled(Mini2AircraftBridge.Snapshot snapshot) {
        if (!ueBridgeEnabled || !Double.isFinite(snapshot.getLatitude())
                || !Double.isFinite(snapshot.getLongitude())) return;
        long now = SystemClock.elapsedRealtime();
        if (now - lastUeBridgePostElapsedMs < 1_000L) return;
        SurveyExecutionStatus executionStatus = surveySimulatorExecution == null
                ? new SurveyExecutionStatus(SurveyExecutionState.IDLE, 0, null)
                : surveySimulatorExecution.getStatus();
        SurveyUeTelemetry telemetry = new SurveyUeTelemetry(
                System.currentTimeMillis(), snapshot.getLatitude(), snapshot.getLongitude(),
                snapshot.getAltitude(), snapshot.getHeading(), snapshot.getGimbalPitch(),
                snapshot.getSimulatorActive(), snapshot.getSimulatorFlying(),
                executionStatus.getState(), executionStatus.getWaypointIndex());
        if (ueBridgeClient.postTelemetry(resolvedSurveyUeEndpoint(), telemetry, (ok, message) -> {
            if (!ok) runOnUiThread(() -> {
                TextView status = findViewById(R.id.ue_bridge_status);
                status.setText(getString(R.string.hil_telemetry_mirror_failed, message));
                status.setTextColor(getColor(R.color.text_danger));
            });
        })) lastUeBridgePostElapsedMs = now;
    }

    private void auditSurveyRealFlightReadiness() {
        Mini2AircraftBridge.Snapshot snapshot = aircraftSnapshot;
        SurveyRealFlightReadinessReport report = SurveyRealFlightReadiness.INSTANCE.evaluate(
                surveyMission,
                new SurveyRealFlightTelemetry(
                        snapshot.getConnected(),
                        System.currentTimeMillis() - snapshot.getFlightStateUpdatedAtMs() <= 1_500L,
                        snapshot.getFlying(), snapshot.getSimulatorActive(),
                        snapshot.getAircraftBattery(), snapshot.getRcBattery(), snapshot.getRcSignal(),
                        snapshot.getSatellites(), "LEVEL_4".equals(snapshot.getGpsLevel())
                        || "LEVEL_5".equals(snapshot.getGpsLevel()),
                        Double.isFinite(snapshot.getHomeLatitude())
                        && Double.isFinite(snapshot.getHomeLongitude()),
                        snapshot.getLatitude(), snapshot.getLongitude(),
                        snapshot.getGoHomeHeightMeters(), snapshot.getMaxFlightHeightMeters(),
                        snapshot.getMaxFlightRadiusMeters(), snapshot.getMaxFlightRadiusEnabled()),
                // Evidence remains false until signed regression artifacts are implemented.
                new SurveyRealFlightEvidence(false, false, false, false, false));
        StringBuilder text = new StringBuilder(getString(R.string.standard_real_flight_auto_survey_disabled));
        for (SurveyRealFlightBlock block : report.getBlocks()) {
            text.append("\n• ").append(realFlightBlockLabel(block));
        }
        TextView status = findViewById(R.id.survey_real_readiness_status);
        status.setText(text.toString());
        status.setTextColor(getColor(R.color.text_danger));
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_real_readiness_title)
                .setMessage(text.toString())
                .setPositiveButton(R.string.action_got_it, null)
                .show();
        appendLog("SURVEY real-flight readiness audit blocks=" + report.getBlocks()
                + " · NO_UNLOCK");
    }

    private String realFlightBlockLabel(SurveyRealFlightBlock block) {
        switch (block) {
            case MISSION_REQUIRED: return getString(R.string.readiness_valid_mission_required);
            case AIRCRAFT_DISCONNECTED: return getString(R.string.flight_controller_disconnected);
            case TELEMETRY_STALE: return getString(R.string.readiness_telemetry_fresh);
            case AIRCRAFT_MUST_BE_ON_GROUND: return getString(R.string.readiness_ground_required);
            case SIMULATOR_MUST_BE_OFF: return getString(R.string.readiness_simulator_off);
            case BATTERY_BELOW_30_PERCENT: return getString(R.string.readiness_aircraft_battery);
            case RC_BATTERY_BELOW_30_PERCENT: return getString(R.string.readiness_rc_battery);
            case RC_SIGNAL_WEAK: return getString(R.string.readiness_rc_signal);
            case GPS_BELOW_12_SATELLITES: return getString(R.string.readiness_gps_satellites);
            case GPS_SIGNAL_WEAK: return getString(R.string.readiness_gps_signal);
            case HOME_LOCATION_REQUIRED: return getString(R.string.readiness_home_valid);
            case GO_HOME_HEIGHT_NOT_CONFIGURED: return getString(R.string.readiness_rth_configured);
            case GO_HOME_HEIGHT_BELOW_MISSION: return getString(R.string.readiness_rth_above_mission);
            case MAX_FLIGHT_HEIGHT_TOO_LOW: return getString(R.string.readiness_max_height);
            case MAX_FLIGHT_RADIUS_REQUIRED: return getString(R.string.gate_radius_invalid);
            case MAX_FLIGHT_RADIUS_TOO_SMALL: return getString(R.string.readiness_radius_cover);
            case SIMULATOR_REGRESSION_REQUIRED: return getString(R.string.readiness_simulator_regression);
            case FAILSAFE_REGRESSION_REQUIRED: return getString(R.string.readiness_failsafe_regression);
            case FRU_BENCH_VERIFICATION_REQUIRED: return getString(R.string.readiness_fru_bench);
            case CAMERA_CALIBRATION_REQUIRED: return getString(R.string.readiness_camera_calibration);
            case OPERATING_AREA_REVIEW_REQUIRED: return getString(R.string.readiness_operating_area_review);
            default: return block.name();
        }
    }

    private void renderSurveyOverlay() {
        if (amap == null) return;
        if (surveyStartMarker != null) {
            surveyStartMarker.remove();
            surveyStartMarker = null;
        }
        if (surveyCoveragePolygon != null) {
            surveyCoveragePolygon.remove();
            surveyCoveragePolygon = null;
        }
        if (surveyTargetPolygon != null) {
            surveyTargetPolygon.remove();
            surveyTargetPolygon = null;
        }
        if (surveyPolygon != null) {
            surveyPolygon.remove();
            surveyPolygon = null;
        }
        for (Polyline route : surveyRoutes) route.remove();
        surveyRoutes.clear();
        if (surveyExecutionRoute != null) {
            surveyExecutionRoute.remove();
            surveyExecutionRoute = null;
        }
        if (surveyExecutionTargetMarker != null) {
            surveyExecutionTargetMarker.remove();
            surveyExecutionTargetMarker = null;
        }
        renderedSurveyExecutionLegIndex = -1;
        for (Marker marker : surveyVertexMarkers) marker.remove();
        surveyVertexMarkers.clear();
        for (Marker marker : surveyActiveCaptureMarkers) marker.remove();
        surveyActiveCaptureMarkers.clear();
        if (surveyMission != null && !surveyMission.getWaypoints().isEmpty()) {
            List<LatLng> coverageBoundary = new ArrayList<>();
            for (GeoPoint point : SurveyPlanner.INSTANCE.groundCoverage(surveyMission).getBoundary()) {
                coverageBoundary.add(toMapLatLng(point));
            }
            surveyCoveragePolygon = (Polygon) amap.addOverlay(new PolygonOptions()
                    .points(coverageBoundary)
                    .fillColor(0x224ED6A8)
                    .stroke(new Stroke(3, 0xAA4ED6A8))
                    .zIndex(2));
            List<LatLng> targetBoundary = new ArrayList<>();
            for (GeoPoint point : SurveyPlanner.INSTANCE.targetArea(surveyMission).getBoundary()) {
                targetBoundary.add(toMapLatLng(point));
            }
            surveyTargetPolygon = (Polygon) amap.addOverlay(new PolygonOptions()
                    .points(targetBoundary)
                    .fillColor(0x1146A6FF)
                    .stroke(new Stroke(3, 0xFF46A6FF))
                    .zIndex(2));
        }
        if (surveyRoi.size() >= 2) {
            List<LatLng> outline = new ArrayList<>();
            for (GeoPoint point : surveyRoi) outline.add(toMapLatLng(point));
            if (surveyRoi.size() >= 3) {
                surveyPolygon = (Polygon) amap.addOverlay(new PolygonOptions()
                        .points(outline)
                        .fillColor(0x3346A6FF)
                        .stroke(new Stroke(3, 0xFF80C8FF))
                        .zIndex(3));
            } else {
                surveyRoutes.add((Polyline) amap.addOverlay(new PolylineOptions()
                        .points(outline).color(0xFF80C8FF).width(3.0f)));
            }
        }
        for (int index = 0; index < surveyRoi.size(); index++) {
            surveyVertexMarkers.add((Marker) amap.addOverlay(new MarkerOptions()
                    .position(toMapLatLng(surveyRoi.get(index)))
                    .icon(surveyMarkerIcon(String.valueOf(index + 1), 0xFF3478C6))
                    .title(getString(R.string.boundary_point_title, index + 1))));
        }
        if (surveyMission != null && !surveyMission.getWaypoints().isEmpty()) {
            SurveyWaypoint firstWaypoint = surveyMission.getWaypoints().get(0);
            surveyStartMarker = (Marker) amap.addOverlay(new MarkerOptions()
                    .position(toMapLatLng(firstWaypoint.getPoint()))
                    .icon(surveyMarkerIcon("S", 0xFF19A974))
                    .title(getString(R.string.route_start_title,
                            getString(surveyMission.getConstraints().getStartPointMode()
                                    == SurveyStartPointMode.AUTO_NEAREST
                                    ? R.string.start_mode_nearest_auto : R.string.start_mode_fixed_first))));
            if (surveyMission.getActiveMapping() != null) {
                int pointNumber = 0;
                for (SurveyWaypoint waypoint : surveyMission.getWaypoints()) {
                    if (waypoint.getKind()
                            != edu.playground.djivln.survey.SurveyWaypointKind.CAPTURE_POINT) continue;
                    pointNumber++;
                    surveyActiveCaptureMarkers.add((Marker) amap.addOverlay(new MarkerOptions()
                            .position(toMapLatLng(waypoint.getPoint()))
                            .icon(surveyMarkerIcon(String.valueOf(pointNumber), 0xFFFF9F43))
                            .title(getString(R.string.precise_recapture_marker_title,
                                    pointNumber, waypoint.getHeadingDegrees(),
                                    waypoint.getGimbalPitchDegrees()))));
                }
            }
            java.util.Map<Integer, ActiveMappingPassMetadata> activePasses = new java.util.HashMap<>();
            if (surveyMission.getActiveMapping() != null) {
                for (ActiveMappingPassMetadata metadata : surveyMission.getActiveMapping().getPasses()) {
                    activePasses.put(metadata.getPassIndex(), metadata);
                }
            }
            for (SurveyPassWaypoints pass : SurveyMissionKt.surveyPasses(surveyMission)) {
                List<SurveyWaypoint> route = pass.getWaypoints();
                if (route.size() < 2) continue;
                SurveyCaptureView captureView = pass.getStart().getCaptureView();
                ActiveMappingPassMetadata activePass = activePasses.get(pass.getStart().getPassIndex());
                boolean bridge = activePass != null
                        && activePass.getRequiredForReconstructionBridge()
                        && !"HIGH_RISE_SCAN".equals(activePass.getRole());
                int routeColor = bridge ? 0xFF87919C : surveyRouteColor(captureView);
                float routeWidth = bridge ? 2.5f
                        : (captureView == SurveyCaptureView.NADIR ? 4.5f : 3.5f);
                if (surveyMission.getTerrainPlan() == null) {
                    List<LatLng> points = new ArrayList<>();
                    for (SurveyWaypoint waypoint : route) points.add(toMapLatLng(waypoint.getPoint()));
                    surveyRoutes.add((Polyline) amap.addOverlay(new PolylineOptions()
                            .points(points).color(routeColor).width(routeWidth)
                            .dottedLine(bridge)
                            .zIndex(4)));
                } else {
                    double minimum = surveyMission.getTerrainPlan().getMinimumWaypointAltitudeMeters();
                    double maximum = surveyMission.getTerrainPlan().getMaximumWaypointAltitudeMeters();
                    double span = Math.max(1.0e-6, maximum - minimum);
                    for (int index = 1; index < route.size(); index++) {
                        SurveyWaypoint from = route.get(index - 1);
                        SurveyWaypoint to = route.get(index);
                        double midpointAltitude = (from.getPoint().getAltitudeMeters()
                                + to.getPoint().getAltitudeMeters()) * 0.5;
                        int color = TerrainAltitudeLegendView.altitudeColor(
                                (midpointAltitude - minimum) / span);
                        surveyRoutes.add((Polyline) amap.addOverlay(new PolylineOptions()
                                .points(java.util.Arrays.asList(
                                        toMapLatLng(from.getPoint()), toMapLatLng(to.getPoint())))
                                .color(bridge ? routeColor : color)
                                .width(bridge ? routeWidth
                                        : (captureView == SurveyCaptureView.NADIR ? 6.5f : 5.0f))
                                .dottedLine(bridge)
                                .zIndex(4)));
                    }
                }
            }
        }
        updateSurveyTerrainAltitudeLegend();
        renderSurveyExecutionOverlay(true);
        renderSurveyEtaBadge();
    }

    private void renderSurveyExecutionOverlay(boolean force) {
        if (amap == null || surveySimulatorExecution == null || surveyMission == null) return;
        SurveyExecutionState state = surveySimulatorExecution.getStatus().getState();
        if (state != SurveyExecutionState.RUNNING && state != SurveyExecutionState.PAUSED
                && state != SurveyExecutionState.ARMING) return;
        int legIndex = surveySimulatorExecution.getExecutionLegIndex();
        if (!force && legIndex == renderedSurveyExecutionLegIndex) return;
        renderedSurveyExecutionLegIndex = legIndex;
        if (surveyExecutionRoute != null) surveyExecutionRoute.remove();
        if (surveyExecutionTargetMarker != null) surveyExecutionTargetMarker.remove();
        SurveyWaypoint target = surveySimulatorExecution.getCurrentTarget();
        List<LatLng> activeLeg = new ArrayList<>();
        if (Double.isFinite(aircraftSnapshot.getLatitude())
                && Double.isFinite(aircraftSnapshot.getLongitude())) {
            activeLeg.add(toMapLatLng(new GeoPoint(
                    aircraftSnapshot.getLatitude(), aircraftSnapshot.getLongitude(),
                    aircraftSnapshot.getAltitude())));
        }
        activeLeg.add(toMapLatLng(target.getPoint()));
        if (activeLeg.size() >= 2) {
            surveyExecutionRoute = (Polyline) amap.addOverlay(new PolylineOptions()
                    .points(activeLeg).color(0xFF00E5FF).width(8.0f).zIndex(20));
        }
        surveyExecutionTargetMarker = (Marker) amap.addOverlay(new MarkerOptions()
                .position(toMapLatLng(target.getPoint()))
                .icon(surveyMarkerIcon(
                        state == SurveyExecutionState.PAUSED ? "Ⅱ" : "▶",
                        state == SurveyExecutionState.PAUSED ? 0xFFF29A2E : 0xFF00A8C6))
                .zIndex(21)
                .title(state == SurveyExecutionState.PAUSED
                        ? getString(R.string.actual_pause_position_resume_returns_here)
                        : getString(R.string.current_execution_leg,
                                legIndex + 1, surveySimulatorExecution.getExecutionLegCount())));
    }

    private int surveyRouteColor(SurveyCaptureView captureView) {
        switch (captureView) {
            case LOCAL_OBLIQUE: return 0xFFFF9F43;
            case FORWARD_OBLIQUE: return 0xFFFF6B6B;
            case BACKWARD_OBLIQUE: return 0xFFB77BFF;
            case LEFT_OBLIQUE: return 0xFF55D69E;
            case RIGHT_OBLIQUE: return 0xFF55BDEB;
            case NADIR:
            default: return 0xFFFFB547;
        }
    }

    private BitmapDescriptor surveyMarkerIcon(String label, int fillColor) {
        int size = dp(24);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setColor(fillColor);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.42f, paint);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(size * (label.length() > 1 ? 0.38f : 0.50f));
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = size / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(label, size / 2f, baseline, paint);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private LatLng toMapLatLng(GeoPoint wgs84) {
        GeoPoint gcj02 = ChinaCoordinateTransform.INSTANCE.wgs84ToGcj02(wgs84);
        return new LatLng(gcj02.getLatitude(), gcj02.getLongitude());
    }

    private void toggleMapFullscreen() {
        FrameLayout pane = findViewById(R.id.map_pane);
        mapFullscreen = !mapFullscreen;
        FrameLayout.LayoutParams params;
        if (mapFullscreen) {
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP | Gravity.START);
            pane.setClipToOutline(false);
            pane.setBackground(null);
            pane.bringToFront();
        } else {
            params = new FrameLayout.LayoutParams(dp(170), dp(96), Gravity.BOTTOM | Gravity.START);
            params.leftMargin = dp(8);
            params.bottomMargin = dp(8);
            setPanelBackground(R.id.map_pane, getColor(R.color.glass_top), getColor(R.color.glass_bottom),
                    getColor(R.color.glass_stroke_strong), 14);
            pane.setClipToOutline(true);
        }
        pane.setLayoutParams(params);
        if (amap != null) {
            amap.getUiSettings().setCompassEnabled(mapFullscreen);
            mapView.showScaleControl(mapFullscreen);
        }
        updateCameraMapPictureInPicture();
        ((Button) findViewById(R.id.map_toggle_button)).setText(mapFullscreen
                ? getString(R.string.action_back) : getString(R.string.action_fullscreen_compact));
        findViewById(R.id.map_toggle_button).setContentDescription(
                mapFullscreen ? getString(R.string.exit_fullscreen_map)
                        : getString(R.string.open_fullscreen_map));
        if (surveyPlanningActive) findViewById(R.id.survey_planner_panel).bringToFront();
        pane.post(() -> {
            if (!surveyPlanningActive) {
                frameMapNearBestLiveLocation(mapFullscreen ? "map open" : "thumbnail restore",
                        mapFullscreen ? 17.5f : 16.8f, true, true);
            }
        });
    }

    private void updateCameraMapPictureInPicture() {
        TextureView video = findViewById(R.id.video_surface);
        View mockBackground = findViewById(R.id.mock_video_background);
        TextView mockLabel = findViewById(R.id.mock_video_label);
        View reticle = findViewById(R.id.center_reticle);
        if (video == null) return;
        FrameLayout.LayoutParams videoParams;
        if (mapFullscreen) {
            videoParams = new FrameLayout.LayoutParams(dp(240), dp(135),
                    Gravity.BOTTOM | Gravity.START);
            videoParams.leftMargin = dp(16);
            videoParams.bottomMargin = dp(16);
            video.setElevation(dp(20));
            if (mockBackground != null) mockBackground.setElevation(dp(21));
            if (surveyPlanningActive) findViewById(R.id.survey_planner_panel).bringToFront();
        } else {
            videoParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            video.setElevation(0f);
            if (mockBackground != null) mockBackground.setElevation(0f);
        }
        video.setLayoutParams(videoParams);
        if (mockBackground != null) mockBackground.setLayoutParams(new FrameLayout.LayoutParams(videoParams));
        refreshCameraPictureInPictureAvailability();
        if (mockLabel != null) mockLabel.setVisibility(mapFullscreen ? View.GONE : mockLabel.getVisibility());
        if (reticle != null) reticle.setVisibility(mapFullscreen ? View.GONE : View.VISIBLE);
    }

    private boolean hasUsableCameraPictureInPicture() {
        if (mockUiActive) return !mockCameraUnavailable;
        long ageMs = SystemClock.elapsedRealtime() - lastFrameAtElapsedMs;
        return aircraftSnapshot.getConnected() && packetCount > 0L && ageMs >= 0L && ageMs <= 2_000L;
    }

    private void refreshCameraPictureInPictureAvailability() {
        TextureView video = findViewById(R.id.video_surface);
        if (video == null) return;
        boolean interactive = !mapFullscreen || hasUsableCameraPictureInPicture();
        // A stale camera PIP still wins touch hit-testing. Hide it so gestures reach the map.
        video.setVisibility(interactive ? View.VISIBLE : View.INVISIBLE);
        video.setClickable(mapFullscreen && interactive);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showAdvancedPanel(boolean visible) {
        View panel = findViewById(R.id.advanced_panel);
        panel.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            panel.bringToFront();
            suppressUnderlyingAccessibility(panel, true);
        } else {
            suppressUnderlyingAccessibility(panel, false);
        }
    }

    private void selectSettingsTab(String tab) {
        settingsTab = tab;
        findViewById(R.id.settings_content_link).setVisibility("link".equals(tab) ? View.VISIBLE : View.GONE);
        findViewById(R.id.settings_content_flight).setVisibility("flight".equals(tab) ? View.VISIBLE : View.GONE);
        findViewById(R.id.settings_content_exec).setVisibility("exec".equals(tab) ? View.VISIBLE : View.GONE);
        findViewById(R.id.settings_content_system).setVisibility("system".equals(tab) ? View.VISIBLE : View.GONE);
        styleSettingsTab(R.id.settings_tab_link, "link".equals(tab));
        styleSettingsTab(R.id.settings_tab_flight, "flight".equals(tab));
        styleSettingsTab(R.id.settings_tab_exec, "exec".equals(tab));
        styleSettingsTab(R.id.settings_tab_system, "system".equals(tab));
    }

    private void styleSettingsTab(int id, boolean selected) {
        Button button = findViewById(id);
        styleButton(button,
                selected ? getColor(R.color.btn_primary_fill) : 0x00000000,
                selected ? getColor(R.color.btn_primary_stroke) : 0x00000000, 10);
        button.setTextColor(getColor(selected ? R.color.text_primary : R.color.text_secondary));
    }

    private void showMonitor(boolean visible) {
        View panel = findViewById(R.id.model_monitor_panel);
        panel.setVisibility(visible ? View.VISIBLE : View.GONE);
        updateMonitorAndFlightPanelLayout();
        if (visible) {
            panel.bringToFront();
            if (monitorFollow) scrollLogToLatest();
        }
    }

    private void showGallery(boolean visible) {
        View panel = findViewById(R.id.gallery_panel);
        panel.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            panel.bringToFront();
            suppressUnderlyingAccessibility(panel, true);
        } else if (aircraftBridge != null) {
            suppressUnderlyingAccessibility(panel, false);
            aircraftBridge.exitMediaMode();
            mainHandler.postDelayed(this::resetVideoFeed, 700L);
        } else {
            suppressUnderlyingAccessibility(panel, false);
        }
    }

    private void suppressUnderlyingAccessibility(View overlay, boolean suppressed) {
        ViewGroup root = findViewById(R.id.root_hud);
        for (int index = 0; index < root.getChildCount(); index++) {
            View child = root.getChildAt(index);
            if (child == overlay) {
                child.setImportantForAccessibility(suppressed
                        ? View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
            } else {
                child.setImportantForAccessibility(suppressed
                        ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
                if (suppressed
                        && child.getId() != R.id.video_surface
                        && child.getId() != R.id.mock_video_background) {
                    modalHiddenVisibilities.putIfAbsent(child, child.getVisibility());
                    if (child.getVisibility() == View.VISIBLE) child.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (suppressed) {
            overlay.setVisibility(View.VISIBLE);
            overlay.bringToFront();
            overlay.requestFocus();
        } else {
            for (Map.Entry<View, Integer> entry : modalHiddenVisibilities.entrySet()) {
                entry.getKey().setVisibility(entry.getValue());
            }
            modalHiddenVisibilities.clear();
        }
    }

    private void initMediaGrid() {
        GridView grid = findViewById(R.id.gallery_grid);
        mediaAdapter = new BaseAdapter() {
            @Override public int getCount() { return mediaShown.size(); }
            @Override public Object getItem(int position) { return mediaShown.get(position); }
            @Override public long getItemId(int position) { return position; }
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View cell = convertView != null ? convertView
                        : getLayoutInflater().inflate(R.layout.item_media_grid, parent, false);
                MediaFile file = mediaShown.get(position);
                String key = file.getFileName();
                ImageView thumb = cell.findViewById(R.id.media_thumb);
                Bitmap cached = mediaThumbs.get(key);
                thumb.setImageBitmap(cached);
                if (cached == null && mediaThumbPending.add(key)) {
                    aircraftBridge.fetchMediaThumbnail(file, bitmap -> runOnUiThread(() -> {
                        mediaThumbPending.remove(key);
                        if (bitmap != null) {
                            mediaThumbs.put(key, bitmap);
                            mediaAdapter.notifyDataSetChanged();
                        } else {
                            appendLog("CAMERA thumbnail unavailable for " + key);
                        }
                    }));
                }
                TextView duration = cell.findViewById(R.id.media_duration);
                if (isVideoFile(file)) {
                    duration.setText("▶ " + formatDuration(Math.round(file.getDurationInSeconds())));
                    duration.setVisibility(View.VISIBLE);
                } else {
                    duration.setVisibility(View.GONE);
                }
                return cell;
            }
        };
        grid.setAdapter(mediaAdapter);
        grid.setOnItemClickListener((parent, view, position, id) -> {
            MediaFile file = mediaShown.get(position);
            double megabytes = file.getFileSize() / 1_048_576.0;
            showBanner(String.format(Locale.US, "%s · %.1f MB · %s", file.getFileName(), megabytes, file.getDateCreated()));
        });
        renderMediaFilterButtons();
    }

    private void setMediaFilter(String filter) {
        mediaFilter = filter;
        applyMediaFilter();
    }

    private void applyMediaFilter() {
        mediaShown.clear();
        for (MediaFile file : mediaFiles) {
            if ("photo".equals(mediaFilter) && isVideoFile(file)) continue;
            if ("video".equals(mediaFilter) && !isVideoFile(file)) continue;
            mediaShown.add(file);
        }
        if (mediaAdapter != null) mediaAdapter.notifyDataSetChanged();
        renderMediaFilterButtons();
        TextView status = findViewById(R.id.gallery_list_text);
        if (!mediaFiles.isEmpty() && mediaShown.isEmpty()) {
            status.setText(R.string.gallery_no_media_in_filter);
            status.setVisibility(View.VISIBLE);
        } else if (!mediaFiles.isEmpty()) {
            status.setVisibility(View.GONE);
        }
    }

    private void renderMediaFilterButtons() {
        styleFilterButton(R.id.gallery_filter_all, "all".equals(mediaFilter));
        styleFilterButton(R.id.gallery_filter_photo, "photo".equals(mediaFilter));
        styleFilterButton(R.id.gallery_filter_video, "video".equals(mediaFilter));
    }

    private void styleFilterButton(int id, boolean selected) {
        styleButton(findViewById(id),
                selected ? getColor(R.color.btn_primary_fill) : getColor(R.color.btn_neutral_fill),
                selected ? getColor(R.color.btn_primary_stroke) : getColor(R.color.btn_neutral_stroke), 10);
    }

    private boolean isVideoFile(MediaFile file) {
        String type = String.valueOf(file.getMediaType()).toUpperCase(Locale.US);
        return type.contains("MOV") || type.contains("MP4") || type.contains("M4V") || type.contains("VIDEO");
    }

    private void refreshGallery() {
        TextView status = findViewById(R.id.gallery_list_text);
        status.setText(R.string.gallery_loading_media);
        status.setVisibility(View.VISIBLE);
        appendLog("CAMERA refresh media list");
        aircraftBridge.fetchMediaFiles((files, error) -> runOnUiThread(() -> {
            if (error != null) {
                status.setText(error);
                appendLog("CAMERA media list error: " + error);
                return;
            }
            mediaFiles.clear();
            mediaFiles.addAll(files);
            mediaThumbs.clear();
            mediaThumbPending.clear();
            status.setVisibility(files.isEmpty() ? View.VISIBLE : View.GONE);
            if (files.isEmpty()) status.setText(R.string.gallery_storage_empty);
            applyMediaFilter();
            appendLog("CAMERA media files: " + files.size());
        }));
    }

    private void toggleVisibility(View view) {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        if (view.getVisibility() == View.VISIBLE) view.bringToFront();
    }

    private void appendLog(String message) {
        String line = String.format(Locale.US, "%tT.%<tL  %s\n", System.currentTimeMillis(), message);
        Log.i(TAG, message);
        if (activityDestroyed) return;
        persistLogLine(line);
        runOnUiThread(() -> {
            runtimeLog.append(line);
            runtimeLogLines++;
            while (runtimeLogLines > 80) {
                int newline = runtimeLog.indexOf("\n");
                if (newline < 0) break;
                runtimeLog.delete(0, newline + 1);
                runtimeLogLines--;
            }
            if (logText != null) logText.setText(runtimeLog.toString());
            if (monitorFollow) scrollLogToLatest();
        });
    }

    private void renderMonitorFollowButton() {
        Button follow = findViewById(R.id.monitor_follow_button);
        if (follow != null) follow.setText(monitorFollow
                ? getString(R.string.logs_follow_on) : getString(R.string.logs_follow_off));
    }

    private void scrollLogToLatest() {
        if (!monitorFollow || logScrollScheduled) return;
        ScrollView scroll = findViewById(R.id.log_scroll);
        if (scroll == null || logText == null) return;
        logScrollScheduled = true;
        // Wait until setText() has completed measurement, then scroll by content
        // coordinates. Focus-based fullScroll can jump back to the selectable
        // TextView's selection at offset zero during a relayout.
        scroll.post(() -> scroll.postOnAnimation(() -> {
            logScrollScheduled = false;
            if (!monitorFollow || logText == null) return;
            int bottom = Math.max(0, logText.getHeight() - scroll.getHeight());
            logAutoScrollInProgress = true;
            scroll.scrollTo(0, bottom);
            logAutoScrollInProgress = false;
        }));
    }

    private synchronized void initPublicCaptureStorage() {
        if (storageInitQueued || sessionLogUri != null || legacySessionLogFile != null) return;
        storageInitQueued = true;
        storageExecutor.execute(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "DJI_VLN_" + storageSessionId + ".log");
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + "/DJI-VLN/logs");
                    sessionLogUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (sessionLogUri == null) throw new IllegalStateException("cannot create public session log");
                } else {
                    File directory = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS), "DJI-VLN/logs");
                    if (!directory.exists() && !directory.mkdirs()) {
                        throw new IllegalStateException("cannot create " + directory);
                    }
                    legacySessionLogFile = new File(directory, "DJI_VLN_" + storageSessionId + ".log");
                }
                writePersistentLogLine(String.format(Locale.US,
                        "%tF %<tT.%<tL  SESSION start · logs=Download/DJI-VLN/logs · images=Download/DJI-VLN/images\n",
                        System.currentTimeMillis()));
                Log.i(TAG, "public capture storage ready: Download/DJI-VLN");
            } catch (Throwable error) {
                Log.e(TAG, "public capture storage init failed", error);
                synchronized (Mini2CameraActivity.this) { storageInitQueued = false; }
            }
        });
    }

    private void persistLogLine(String line) {
        if (activityDestroyed || storageExecutor.isShutdown() ||
                (!storageInitQueued && sessionLogUri == null && legacySessionLogFile == null)) return;
        try {
            storageExecutor.execute(() -> {
                try {
                    writePersistentLogLine(line);
                } catch (Throwable error) {
                    Log.e(TAG, "persist log failed", error);
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            // Activity recreation can race a final DJI callback with executor shutdown.
        }
    }

    private void writePersistentLogLine(String line) throws Exception {
        byte[] data = line.getBytes(StandardCharsets.UTF_8);
        Uri uri = sessionLogUri;
        if (uri != null) {
            try (OutputStream output = getContentResolver().openOutputStream(uri, "wa")) {
                if (output == null) throw new IllegalStateException("cannot open session log");
                output.write(data);
            }
            return;
        }
        File file = legacySessionLogFile;
        if (file != null) {
            try (OutputStream output = new FileOutputStream(file, true)) {
                output.write(data);
            }
        }
    }

    private void queueInferenceFrameSave(Bitmap bitmap, String prompt) {
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, encoded)) {
            Log.e(TAG, "inference frame JPEG encode failed");
            return;
        }
        byte[] jpeg = encoded.toByteArray();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        String fileName = "VLN_" + timestamp + ".jpg";
        storageExecutor.execute(() -> {
            try {
                String visiblePath = savePublicInferenceImage(fileName, jpeg);
                writePersistentLogLine(String.format(Locale.US,
                        "%tF %<tT.%<tL  FRAME saved=%s bytes=%d prompt=%s\n",
                        System.currentTimeMillis(), visiblePath, jpeg.length,
                        prompt.replace('\n', ' ').replace('\r', ' ')));
            } catch (Throwable error) {
                Log.e(TAG, "save inference frame failed", error);
                try {
                    writePersistentLogLine(String.format(Locale.US,
                            "%tF %<tT.%<tL  FRAME save failed: %s\n",
                            System.currentTimeMillis(), error.getMessage()));
                } catch (Throwable ignored) {}
            }
        });
    }

    private String savePublicInferenceImage(String fileName, byte[] jpeg) throws Exception {
        return savePublicDownloadFile("images", fileName, "image/jpeg", jpeg);
    }

    private String savePublicDownloadFile(
            String relativeDirectory, String fileName, String mimeType, byte[] data) throws Exception {
        String relative = Environment.DIRECTORY_DOWNLOADS + "/DJI-VLN/" + relativeDirectory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relative);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IllegalStateException("cannot create public file");
            try {
                try (OutputStream output = getContentResolver().openOutputStream(uri, "w")) {
                    if (output == null) throw new IllegalStateException("cannot open public file");
                    output.write(data);
                }
                ContentValues ready = new ContentValues();
                ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContentResolver().update(uri, ready, null, null);
            } catch (Exception error) {
                getContentResolver().delete(uri, null, null);
                throw error;
            }
            return relative + "/" + fileName;
        }
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "DJI-VLN/" + relativeDirectory);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("cannot create " + directory);
        }
        File file = new File(directory, fileName);
        try (OutputStream output = new FileOutputStream(file)) {
            output.write(data);
        }
        return "Download/DJI-VLN/" + relativeDirectory + "/" + fileName;
    }

    private String safeStorageName(String value) {
        String sanitized = value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isEmpty() ? "unknown" : sanitized;
    }

    private void showBanner(String message) {
        runOnUiThread(() -> {
            toastBanner.setText(message);
            toastBanner.setVisibility(View.VISIBLE);
            toastBanner.bringToFront();
            mainHandler.removeCallbacks(hideBannerRunnable);
            mainHandler.postDelayed(hideBannerRunnable, 2_800L);
        });
    }

    private final Runnable hideBannerRunnable = () -> {
        if (toastBanner != null) toastBanner.setVisibility(View.GONE);
    };

    private void showPhotoCaptureFeedback(boolean success) {
        if (surveyPhotoCaptureFeedback == null) return;
        mainHandler.removeCallbacks(hidePhotoCaptureFeedbackRunnable);
        surveyPhotoCaptureFeedback.animate().cancel();
        surveyPhotoCaptureFeedback.setText(success
                ? R.string.captured_indicator : R.string.capture_failed_indicator);
        surveyPhotoCaptureFeedback.setTextColor(success
                ? getColor(R.color.text_primary) : getColor(R.color.text_danger));
        surveyPhotoCaptureFeedback.setAlpha(0f);
        surveyPhotoCaptureFeedback.setVisibility(View.VISIBLE);
        surveyPhotoCaptureFeedback.bringToFront();
        surveyPhotoCaptureFeedback.animate().alpha(1f).setDuration(80L).start();
        mainHandler.postDelayed(hidePhotoCaptureFeedbackRunnable, success ? 520L : 1_380L);
    }

    private final Runnable hidePhotoCaptureFeedbackRunnable = () -> {
        if (surveyPhotoCaptureFeedback == null) return;
        surveyPhotoCaptureFeedback.animate().cancel();
        surveyPhotoCaptureFeedback.animate().alpha(0f).setDuration(180L)
                .withEndAction(() -> surveyPhotoCaptureFeedback.setVisibility(View.GONE)).start();
    };

    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void allowContentAcrossDisplayCutout() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return;
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else {
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(attributes);
    }

    private void selectModelTransport(ModelTransport transport) {
        if (transport != modelTransport
                && (controlArmed || autoInferenceEnabled || inferenceInFlight
                || chunkExecutionActive || activeRelativeMoveRunnable != null)) {
            normalStop(getString(R.string.reason_switch_model_transport));
        }
        modelTransport = transport;
        if (transport == ModelTransport.USB) {
            ensureAoaUsbProbe().start();
        } else if (aoaUsbProbe != null) {
            aoaUsbProbe.disable();
        }
        ethernetEndpoint.setVisibility(transport == ModelTransport.ETHERNET ? View.VISIBLE : View.GONE);
        findViewById(R.id.import_model).setVisibility(transport == ModelTransport.LOCAL ? View.VISIBLE : View.GONE);
        findViewById(R.id.download_cloud_model).setVisibility(transport == ModelTransport.LOCAL ? View.VISIBLE : View.GONE);
        findViewById(R.id.denoise_steps_button).setVisibility(transport == ModelTransport.LOCAL ? View.VISIBLE : View.GONE);
        localTransportButton.setAlpha(transport == ModelTransport.LOCAL ? 1.0f : 0.55f);
        usbTransportButton.setAlpha(transport == ModelTransport.USB ? 1.0f : 0.55f);
        ethernetTransportButton.setAlpha(transport == ModelTransport.ETHERNET ? 1.0f : 0.55f);
        renderTransportStyles();
        renderModelStatus(transportLabel() + " · " + (transport == ModelTransport.LOCAL
                ? localRuntime().activeModelDescription() : getString(R.string.waiting_for_connection)));
    }

    private String transportLabel() {
        if (modelTransport == ModelTransport.LOCAL) return "LOCAL";
        if (modelTransport == ModelTransport.USB) return "USB AOA";
        return "ETHERNET";
    }

    private synchronized Mini2OpenFlyRuntime localRuntime() {
        if (localOpenFlyRuntime == null) {
            localOpenFlyRuntime = new Mini2OpenFlyRuntime(getApplicationContext());
            localOpenFlyRuntime.updateTelemetry(aircraftSnapshot);
        }
        return localOpenFlyRuntime;
    }

    private synchronized AoaUsbProbe ensureAoaUsbProbe() {
        if (aoaUsbProbe == null) {
            aoaUsbProbe = new AoaUsbProbe(getApplicationContext(), message -> {
                runOnUiThread(() -> {
                    renderModelStatus("USB AOA · " + message);
                    modelResultText.setText(message);
                });
                return Unit.INSTANCE;
            });
        }
        return aoaUsbProbe;
    }

    private void chooseModelPack() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityForResult(intent, MODEL_PACK_REQUEST);
    }

    private void chooseSurveyMission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String lastUri = getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE)
                    .getString(SURVEY_LAST_JSON_DOCUMENT_URI_KEY,
                            "content://com.android.externalstorage.documents/document/primary%3ADownload");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(lastUri));
        }
        startActivityForResult(intent, SURVEY_MISSION_IMPORT_REQUEST);
    }

    private void chooseSurveyDsm() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/tiff");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/tiff", "image/geotiff",
                "application/geotiff", "application/octet-stream"});
        startActivityForResult(intent, SURVEY_DSM_IMPORT_REQUEST);
    }

    private void chooseSurveyBuildingHeight() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/tiff");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/tiff", "image/geotiff",
                "application/geotiff", "application/octet-stream"});
        startActivityForResult(intent, SURVEY_BUILDING_HEIGHT_IMPORT_REQUEST);
    }

    private void exportSurveyMission() {
        if (surveyMission == null) {
            renderSurveyStatus(getString(R.string.survey_export_requires_route));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "openfly-survey-" + storageSessionId + ".json");
        startActivityForResult(intent, SURVEY_MISSION_EXPORT_REQUEST);
    }

    private void downloadLatestCloudModel() {
        if (modelTransport != ModelTransport.LOCAL) {
            showBanner(getString(R.string.model_select_local_first));
            return;
        }
        if (!beginModelMaintenance(getString(R.string.download_cloud_model_operation))) return;
        cloudModelDownloadInFlight = true;
        modelLoaded = false;
        lastCloudProgressPhase = "";
        lastCloudProgressBucket = -1;
        cloudModelButton.setEnabled(false);
        cloudModelButton.setText(R.string.model_reading_cloud_catalog);
        modelResultText.setText(R.string.model_reading_azure_catalog);
        appendLog(getString(R.string.model_cloud_download_started_log));
        modelExecutor.execute(() -> {
            try {
                String description = localRuntime().importLatestCloudModel(this::updateCloudModelProgress);
                InferenceControlClient.Result loadResult = localRuntime().getControlClient().load();
                runOnUiThread(() -> {
                    renderModelStatus("LOCAL · " + description);
                    modelResultText.setText(getString(R.string.model_cloud_ready, description));
                    appendLog(getString(R.string.model_cloud_import_complete_log, description));
                    showModelResult("load", loadResult);
                    showBanner(getString(loadResult.getOk()
                            ? R.string.model_cloud_loaded : R.string.model_cloud_load_failed));
                });
            } catch (Throwable error) {
                showModelFailure(getString(R.string.model_failure_cloud), error, false);
            } finally {
                runOnUiThread(() -> {
                    cloudModelDownloadInFlight = false;
                    cloudModelButton.setEnabled(true);
                    cloudModelButton.setText(getString(R.string.model_download_uavflow));
                    endModelMaintenance();
                });
            }
        });
    }

    private void updateCloudModelProgress(String phase, long completedBytes, long totalBytes) {
        int percent = totalBytes > 0L
                ? (int) Math.max(0L, Math.min(100L, completedBytes * 100L / totalBytes))
                : -1;
        int bucket = percent < 0 ? -1 : percent / 10;
        boolean shouldLog = !phase.equals(lastCloudProgressPhase)
                || bucket != lastCloudProgressBucket
                || percent == 100;
        lastCloudProgressPhase = phase;
        lastCloudProgressBucket = bucket;
        runOnUiThread(() -> {
            String label = percent >= 0 ? phase + " " + percent + "%" : phase + "…";
            cloudModelButton.setText(label);
            modelResultText.setText(getString(R.string.model_cloud_status, label));
            if (shouldLog) appendLog(getString(R.string.model_cloud_log, label));
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        if (requestCode == SURVEY_MISSION_IMPORT_REQUEST) {
            try {
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Throwable ignored) {
                // Some document providers do not offer persistable grants; the location is still useful.
            }
            getSharedPreferences(SURVEY_SESSION_PREFERENCES, MODE_PRIVATE).edit()
                    .putString(SURVEY_LAST_JSON_DOCUMENT_URI_KEY, uri.toString()).apply();
            importSurveyMission(uri);
            return;
        }
        if (requestCode == SURVEY_MISSION_EXPORT_REQUEST) {
            writeSurveyMission(uri);
            return;
        }
        if (requestCode == SURVEY_DSM_IMPORT_REQUEST) {
            importSurveyDsm(uri);
            return;
        }
        if (requestCode == SURVEY_BUILDING_HEIGHT_IMPORT_REQUEST) {
            importSurveyBuildingHeight(uri);
            return;
        }
        if (requestCode == V86_OFFLINE_FOLDER_REQUEST) {
            replayV86OfflineFolder(uri);
            return;
        }
        if (requestCode != MODEL_PACK_REQUEST) return;
        if (!beginModelMaintenance(getString(R.string.import_local_model_pack_operation))) return;
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            Log.w(TAG, "model URI permission is not persistable", ignored);
        }
        modelResultText.setText(R.string.model_import_verifying);
        modelExecutor.execute(() -> {
            try {
                String description = localRuntime().importModel(uri);
                InferenceControlClient.Result loadResult = localRuntime().getControlClient().load();
                runOnUiThread(() -> {
                    renderModelStatus("LOCAL · " + description);
                    modelResultText.setText(getString(R.string.model_import_success, description));
                    appendLog(getString(R.string.model_import_success_log, description));
                    showModelResult("load", loadResult);
                });
            } catch (Throwable error) {
                showModelFailure(getString(R.string.model_failure_import), error, false);
            } finally {
                runOnUiThread(this::endModelMaintenance);
            }
        });
    }

    private void importSurveyDsm(Uri uri) {
        final List<GeoPoint> roiSnapshot = !surveyRoi.isEmpty()
                ? new ArrayList<>(surveyRoi)
                : surveyMission == null ? java.util.Collections.emptyList()
                        : new ArrayList<>(surveyMission.getRoi());
        if (roiSnapshot.size() < 3) {
            renderSurveyStatus(getString(R.string.terrain_draw_boundary_first));
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            Log.w(TAG, "DSM URI permission is not persistable", ignored);
        }
        TextView status = findViewById(R.id.survey_dsm_status);
        status.setText(R.string.terrain_reading_geotiff);
        storageExecutor.execute(() -> {
            try {
                byte[] bytes = readUriBytes(uri, 96L * 1024L * 1024L);
                String name = displayName(uri);
                GeoTiffTerrain terrain = GeoTiffTerrain.Companion.read(
                        new ByteArrayInputStream(bytes), name, this);
                TerrainImportSafety.INSTANCE.requireCompleteCoverage(
                        terrain, roiSnapshot, this);
                String digest = SurveyTerrainPlanner.INSTANCE.sha256(bytes);
                double[] preview = terrain.previewGrid(96, 64);
                runOnUiThread(() -> {
                    surveyTerrain = terrain;
                    surveyGlobalTerrainBase = null;
                    surveyTerrainSha256 = digest;
                    surveyTerrainPreviewGrid = preview;
                    findViewById(R.id.survey_dsm_building_confirm).setEnabled(true);
                    TerrainPreviewView view = findViewById(R.id.survey_terrain_preview);
                    showSurveyTerrainPreview(new TerrainPreviewData(
                            terrain.getInfo(), preview, 96, 64));
                    boolean missionCovered = isSurveyMissionCoveredByTerrain(surveyMission, terrain.getInfo());
                    view.showMission(missionCovered ? surveyMission : null);
                    String model = getString(((Spinner) findViewById(R.id.survey_terrain_kind_spinner))
                            .getSelectedItemPosition() == 0
                            ? R.string.dsm_with_buildings_canopy : R.string.dem_dtm_without_buildings);
                    TerrainRasterInfo info = terrain.getInfo();
                    String pixelSize;
                    if (info.getEpsg() == 4326) {
                        double middleLatitude = (info.getMinimumLatitude() + info.getMaximumLatitude()) * 0.5;
                        double metersPerDegree = 111_320.0;
                        double pixelMetersX = info.getPixelSizeX() * metersPerDegree
                                * Math.cos(Math.toRadians(middleLatitude));
                        double pixelMetersY = info.getPixelSizeY() * metersPerDegree;
                        pixelSize = getString(R.string.pixel_size_degrees_and_meters,
                                info.getPixelSizeX(), info.getPixelSizeY(), pixelMetersX, pixelMetersY);
                    } else {
                        pixelSize = String.format(Locale.US, "%.2f×%.2f m",
                                info.getPixelSizeX(), info.getPixelSizeY());
                    }
                    status.setText(getString(R.string.terrain_import_details,
                            name, model, info.getEpsg(), info.getWidth(), info.getHeight(), pixelSize,
                            info.getNoDataValue() == null ? getString(R.string.not_declared) :
                                    String.valueOf(info.getNoDataValue()),
                            digest.substring(0, 12)));
                    renderSurveyStatus(getString(missionCovered
                            ? R.string.dsm_imported_regenerate_route
                            : R.string.dsm_imported_roi_outside_coverage));
                    appendLog("SURVEY DSM loaded " + name + " EPSG:" + terrain.getInfo().getEpsg()
                            + " sha256=" + digest);
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
                    status.setText(getString(R.string.terrain_dsm_import_failed, error.getMessage()));
                    showBanner(getString(R.string.terrain_dsm_geotiff_import_failed));
                    appendLog("SURVEY DSM import failed " + error);
                });
            }
        });
    }

    private void downloadGlobalSurveyTerrain() {
        if (surveyTerrainCalculationInFlight) {
            renderSurveyStatus(getString(R.string.terrain_processing_wait));
            return;
        }
        if (surveyRoi.size() < 3) {
            renderSurveyStatus(getString(R.string.terrain_draw_boundary_first));
            return;
        }
        final List<GeoPoint> roiSnapshot = new ArrayList<>(surveyRoi);
        final TextView status = findViewById(R.id.survey_dsm_status);
        surveyTerrainCalculationInFlight = true;
        status.setText(R.string.terrain_downloading_global_dem);
        renderSurveyStatus(getString(R.string.terrain_global_dem_preview_only));
        storageExecutor.execute(() -> {
            try {
                GlobalTerrainDownloadResult result = new GlobalTerrainDownloader(
                        getApplicationContext()).download(roiSnapshot, 14, (completed, total) -> {
                    runOnUiThread(() -> status.setText(
                            getString(R.string.downloading_global_bare_earth_dem, completed, total)));
                    return kotlin.Unit.INSTANCE;
                });
                TerrainElevationSource terrain = result.getTerrain();
                TerrainImportSafety.INSTANCE.requireCompleteCoverage(
                        terrain, roiSnapshot, this);
                TerrainPreviewData preview = TerrainPreviewSampler.INSTANCE.forArea(
                        terrain, roiSnapshot, 96, 64, this);
                runOnUiThread(() -> {
                    surveyTerrainCalculationInFlight = false;
                    surveyTerrain = terrain;
                    surveyGlobalTerrainBase = terrain;
                    surveyTerrainSha256 = result.getSha256();
                    surveyTerrainPreviewGrid = preview.getElevations();
                    Spinner kind = findViewById(R.id.survey_terrain_kind_spinner);
                    kind.setSelection(1);
                    CheckBox buildingConfirm = findViewById(R.id.survey_dsm_building_confirm);
                    buildingConfirm.setChecked(false);
                    buildingConfirm.setEnabled(false);
                    TerrainPreviewView view = findViewById(R.id.survey_terrain_preview);
                    showSurveyTerrainPreview(preview);
                    boolean missionCovered = isSurveyMissionCoveredByTerrain(
                            surveyMission, terrain.getInfo());
                    view.showMission(missionCovered ? surveyMission : null);
                    TerrainRasterInfo info = terrain.getInfo();
                    double middleLatitude = (info.getMinimumLatitude() +
                            info.getMaximumLatitude()) * 0.5;
                    double pixelMeters = info.getPixelSizeX() * 111_320.0 *
                            Math.cos(Math.toRadians(middleLatitude));
                    status.setText(getString(R.string.global_bare_earth_dem_details,
                            info.getWidth(), info.getHeight(), pixelMeters,
                            result.getDownloadedTiles(), result.getCachedTiles()));
                    renderSurveyStatus(getString(aircraftSnapshot.getConnected()
                            ? R.string.terrain_dem_ready_generate_route
                            : surveyDebugPreviewTakeoffPoint != null
                                    ? R.string.terrain_debug_preview_temp_home
                                    : R.string.terrain_dem_ready_connect_aircraft));
                    appendLog("SURVEY global terrain loaded tiles=" +
                            (result.getDownloadedTiles() + result.getCachedTiles()) +
                            " sha256=" + result.getSha256());
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
                    surveyTerrainCalculationInFlight = false;
                    status.setText(getString(R.string.terrain_global_download_failed, error.getMessage()));
                    renderSurveyStatus(getString(R.string.terrain_global_unavailable));
                    appendLog("SURVEY global terrain failed " + error);
                });
            }
        });
    }

    private void downloadGlobalBuildingHeights() {
        if (surveyTerrainCalculationInFlight) {
            renderSurveyStatus(getString(R.string.terrain_processing_wait));
            return;
        }
        if (surveyRoi.size() < 3) {
            renderSurveyStatus(getString(R.string.terrain_draw_area_before_buildings));
            return;
        }
        if (surveyGlobalTerrainBase == null) {
            renderSurveyStatus(getString(R.string.terrain_download_dem_before_buildings));
            return;
        }
        if (BuildConfig.GLOBAL_BUILDING_HEIGHT_COG_TEMPLATE.trim().isEmpty()) {
            renderSurveyStatus(getString(R.string.terrain_select_building_geotiff));
            chooseSurveyBuildingHeight();
            return;
        }
        final List<GeoPoint> roiSnapshot = new ArrayList<>(surveyRoi);
        final TerrainElevationSource ground = surveyGlobalTerrainBase;
        final String groundSha256 = surveyTerrainSha256;
        final TextView status = findViewById(R.id.survey_dsm_status);
        surveyTerrainCalculationInFlight = true;
        status.setText(R.string.terrain_downloading_buildings);
        storageExecutor.execute(() -> {
            try {
                BuildingHeightDownloadResult result = new GlobalBuildingHeightDownloader(
                        getApplicationContext(), BuildConfig.GLOBAL_BUILDING_HEIGHT_COG_TEMPLATE
                ).download(roiSnapshot);
                TerrainElevationSource surface = new CompositeSurfaceElevationSource(
                        ground, result.getHeightAboveGround(),
                        getString(R.string.global_surface_dsm_building_atlas), this);
                TerrainImportSafety.INSTANCE.requireCompleteCoverage(
                        result.getHeightAboveGround(), roiSnapshot, this);
                String combinedSha256 = SurveyTerrainPlanner.INSTANCE.sha256(
                        (groundSha256 + result.getSha256()).getBytes(StandardCharsets.UTF_8));
                TerrainPreviewData preview = TerrainPreviewSampler.INSTANCE.forArea(
                        surface, roiSnapshot, 96, 64, this);
                runOnUiThread(() -> {
                    surveyTerrainCalculationInFlight = false;
                    surveyTerrain = surface;
                    surveyTerrainSha256 = combinedSha256;
                    surveyTerrainPreviewGrid = preview.getElevations();
                    ((Spinner) findViewById(R.id.survey_terrain_kind_spinner)).setSelection(0);
                    CheckBox confirm = findViewById(R.id.survey_dsm_building_confirm);
                    confirm.setChecked(false);
                    confirm.setEnabled(true);
                    TerrainPreviewView view = findViewById(R.id.survey_terrain_preview);
                    showSurveyTerrainPreview(preview);
                    view.showMission(isSurveyMissionCoveredByTerrain(
                            surveyMission, surface.getInfo()) ? surveyMission : null);
                    status.setText(getString(R.string.global_surface_dsm_details,
                            result.getTileCount(),
                            result.getSourceVersion() == null
                                    ? getString(R.string.version_not_declared) : result.getSourceVersion(),
                            result.getBytes() / 1048576.0,
                            getString(result.getCached() ? R.string.cache_source : R.string.download_source)));
                    renderSurveyStatus(getString(aircraftSnapshot.getConnected()
                            ? R.string.terrain_buildings_ready_generate_route
                            : R.string.terrain_buildings_ready_connect_aircraft));
                    appendLog("SURVEY global building height loaded cached=" +
                            result.getCached() + " sha256=" + combinedSha256);
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
                    surveyTerrainCalculationInFlight = false;
                    status.setText(getString(R.string.terrain_building_enhancement_failed,
                            error.getMessage()));
                    renderSurveyStatus(getString(R.string.terrain_buildings_unavailable_dem_kept));
                    appendLog("SURVEY global building height failed " + error);
                });
            }
        });
    }

    private void importSurveyBuildingHeight(Uri uri) {
        if (surveyGlobalTerrainBase == null || surveyRoi.size() < 3) {
            renderSurveyStatus(getString(R.string.terrain_draw_area_download_dem_first));
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            Log.w(TAG, "building height URI permission is not persistable", ignored);
        }
        final List<GeoPoint> roiSnapshot = new ArrayList<>(surveyRoi);
        final TerrainElevationSource ground = surveyGlobalTerrainBase;
        final String groundSha256 = surveyTerrainSha256;
        final TextView status = findViewById(R.id.survey_dsm_status);
        surveyTerrainCalculationInFlight = true;
        status.setText(R.string.terrain_reading_building_geotiff);
        storageExecutor.execute(() -> {
            try {
                byte[] bytes = readUriBytes(uri, 128L * 1024L * 1024L);
                String name = displayName(uri);
                GeoTiffTerrain buildingHeight = GeoTiffTerrain.Companion.read(
                        new ByteArrayInputStream(bytes), name, this);
                TerrainImportSafety.INSTANCE.requireCompleteCoverage(
                        buildingHeight, roiSnapshot, this);
                TerrainPreviewData heightPreview = buildingHeight.previewForArea(
                        roiSnapshot, 48, 32);
                for (double value : heightPreview.getElevations()) {
                    if (Double.isFinite(value) && value < 0.0) {
                        throw new IllegalArgumentException(getString(R.string.building_relative_height_negative));
                    }
                }
                TerrainElevationSource surface = new CompositeSurfaceElevationSource(
                        ground, buildingHeight,
                        getString(R.string.global_surface_dsm_local_building_height), this);
                String heightSha256 = SurveyTerrainPlanner.INSTANCE.sha256(bytes);
                String combinedSha256 = SurveyTerrainPlanner.INSTANCE.sha256(
                        (groundSha256 + heightSha256).getBytes(StandardCharsets.UTF_8));
                TerrainPreviewData preview = TerrainPreviewSampler.INSTANCE.forArea(
                        surface, roiSnapshot, 96, 64, this);
                runOnUiThread(() -> {
                    surveyTerrainCalculationInFlight = false;
                    surveyTerrain = surface;
                    surveyTerrainSha256 = combinedSha256;
                    surveyTerrainPreviewGrid = preview.getElevations();
                    ((Spinner) findViewById(R.id.survey_terrain_kind_spinner)).setSelection(0);
                    CheckBox confirm = findViewById(R.id.survey_dsm_building_confirm);
                    confirm.setChecked(false);
                    confirm.setEnabled(true);
                    TerrainPreviewView view = findViewById(R.id.survey_terrain_preview);
                    showSurveyTerrainPreview(preview);
                    view.showMission(isSurveyMissionCoveredByTerrain(
                            surveyMission, surface.getInfo()) ? surveyMission : null);
                    status.setText(getString(R.string.terrain_local_building_status,
                            name, combinedSha256.substring(0, 16)));
                    renderSurveyStatus(getString(R.string.terrain_local_building_ready));
                    appendLog("SURVEY local building height loaded sha256=" + combinedSha256);
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
                    surveyTerrainCalculationInFlight = false;
                    status.setText(getString(R.string.terrain_building_import_failed,
                            error.getMessage() == null
                                    ? error.getClass().getSimpleName() : error.getMessage()));
                    renderSurveyStatus(getString(R.string.terrain_building_import_failed_short));
                    appendLog("SURVEY local building height failed " + error);
                });
            }
        });
    }

    private boolean isSurveyMissionCoveredByTerrain(SurveyMission mission, TerrainRasterInfo info) {
        if (mission == null || mission.getRoi().isEmpty()) return false;
        for (edu.playground.djivln.survey.GeoPoint point : mission.getRoi()) {
            if (point.getLatitude() < info.getMinimumLatitude()
                    || point.getLatitude() > info.getMaximumLatitude()
                    || point.getLongitude() < info.getMinimumLongitude()
                    || point.getLongitude() > info.getMaximumLongitude()) {
                return false;
            }
        }
        return true;
    }

    private byte[] readUriBytes(Uri uri, long maximumBytes) throws Exception {
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IllegalArgumentException(getString(R.string.cannot_read_file));
            long effectiveMaximum = Math.min(maximumBytes, TerrainImportSafety.MAX_IMPORT_BYTES);
            return TerrainImportSafety.INSTANCE.readBounded(input, effectiveMaximum, this);
        }
    }

    private String displayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.trim().isEmpty()) return name;
            }
        } catch (Exception ignored) { }
        String last = uri.getLastPathSegment();
        return last == null ? "terrain.tif" : last;
    }

    private void importSurveyMission(Uri uri) {
        storageExecutor.execute(() -> {
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IllegalArgumentException(getString(R.string.cannot_read_mission_file));
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) >= 0) bytes.write(buffer, 0, count);
                SurveyMission imported = SurveyMissionJson.INSTANCE.decode(
                        new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                if (imported.getActiveMapping() != null) {
                    edu.playground.djivln.survey.ActiveRecaptureMissionValidator.validate(imported);
                }
                runOnUiThread(() -> {
                    activateSurveyMission(imported, getString(R.string.imported));
                    saveSurveyMissionVersion(false);
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
            renderSurveyStatus(getString(R.string.survey_import_failed_status, error.getMessage()));
            showBanner(getString(R.string.survey_import_failed_banner));
                    appendLog("SURVEY import failed " + error);
                });
            }
        });
    }

    private void writeSurveyMission(Uri uri) {
        SurveyMission mission = surveyMission;
        if (mission == null) return;
        storageExecutor.execute(() -> {
            try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
                if (output == null) throw new IllegalArgumentException(getString(R.string.cannot_write_mission_file));
                output.write(SurveyMissionJson.INSTANCE.encode(mission).getBytes(StandardCharsets.UTF_8));
                output.flush();
                runOnUiThread(() -> {
            renderSurveyStatus(getString(R.string.survey_exported_status, surveySummary(mission)));
            showBanner(getString(R.string.survey_exported_banner));
                    appendLog("SURVEY exported " + mission.getId());
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
            renderSurveyStatus(getString(R.string.survey_export_failed_status, error.getMessage()));
            showBanner(getString(R.string.survey_export_failed_banner));
                    appendLog("SURVEY export failed " + error);
                });
            }
        });
    }

    private void frameSurveyMission(SurveyMission mission) {
        if (mission == null) return;
        frameSurveyArea(mission.getRoi());
    }

    private void frameCompleteSurvey() {
        if (surveyMission != null) {
            frameSurveyMission(surveyMission);
            renderSurveyStatus(getString(R.string.survey_full_route_shown));
            return;
        }
        if (!surveyRoi.isEmpty()) {
            frameSurveyArea(surveyRoi);
            renderSurveyStatus(getString(R.string.survey_full_area_shown));
            return;
        }
        if (frameMapNearBestLiveLocation("survey full-route fallback", 18.0f,
                true, true)) {
                renderSurveyStatus(getString(R.string.survey_no_area_focused_live));
        } else {
                renderSurveyStatus(getString(R.string.survey_no_area_no_location));
        }
    }

    private void frameSurveyArea(List<GeoPoint> roi) {
        if (amap == null || roi == null || roi.isEmpty()) return;
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (GeoPoint point : roi) {
            bounds.include(toMapLatLng(point));
        }
        mapInitiallyFramed = true;
        // Keep the complete mission in the visible map area left of the Pilot-style drawer.
        int rightPadding = surveyPlanningActive ? dp(340) : dp(40);
        amap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(
                bounds.build(), dp(70), dp(100), rightPadding, dp(80)), 500);
    }

    private void runModelOperation(String operation) {
        boolean maintenance = "health".equals(operation)
                || "load".equals(operation) || "reset".equals(operation);
        if (maintenance && !beginModelMaintenance(getString(R.string.model_operation, operation))) return;
        if (!maintenance && modelMaintenanceInFlight && !"stop".equals(operation)) {
            showBanner(getString(R.string.model_maintenance_wait));
            return;
        }
        if ("load".equals(operation)) {
            modelLoadPollAttempts = 0;
            mainHandler.removeCallbacks(modelLoadPollRunnable);
        }
        final ModelTransport selectedTransport = modelTransport;
        final String endpoint = ethernetEndpoint.getText().toString().trim();
        final String prompt = normalizedPrompt();
        modelResultText.setText(transportLabel() + " · " + operation + "…");
        modelExecutor.execute(() -> {
            try {
                InferenceControlClient client = controlClient(selectedTransport, endpoint);
                InferenceControlClient.Result result;
                switch (operation) {
                    case "health": result = client.health(); break;
                    case "load": result = client.load(); break;
                    case "preflight": result = client.preflight(); break;
                    case "start": result = client.start(prompt); break;
                    case "stop": result = client.stop(); break;
                    case "reset": result = client.reset(); break;
                    default: throw new IllegalArgumentException("unknown operation: " + operation);
                }
                showModelResult(operation, result);
            } catch (Throwable error) {
                showModelFailure(getString(R.string.model_failure_operation, operation), error, false);
            } finally {
                if (maintenance) runOnUiThread(this::endModelMaintenance);
            }
        });
    }

    private boolean beginModelMaintenance(String reason) {
        if (modelMaintenanceInFlight) {
            showBanner(getString(R.string.model_operation_busy));
            return false;
        }
        autoInferenceEnabled = false;
        mainHandler.removeCallbacks(autoInferenceRunnable);
        if (controlArmed || inferenceInFlight || chunkExecutionActive
                || activeRelativeMoveRunnable != null) {
            normalStop(reason);
        }
        modelMaintenanceInFlight = true;
        setModelMaintenanceUi(true);
        appendLog("MODEL maintenance begin · " + reason);
        return true;
    }

    private void endModelMaintenance() {
        if (!modelMaintenanceInFlight) return;
        modelMaintenanceInFlight = false;
        controlArmed = false;
        autoInferenceEnabled = false;
        setModelMaintenanceUi(false);
        appendLog("MODEL maintenance end · control remains disarmed");
        renderSafetyState();
    }

    private void setModelMaintenanceUi(boolean busy) {
        int[] ids = new int[] {
                R.id.model_load_main, R.id.model_health, R.id.model_load,
                R.id.model_preflight, R.id.model_start, R.id.model_reset,
                R.id.download_cloud_model, R.id.import_model, R.id.model_infer_once
        };
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) view.setEnabled(!busy);
        }
        if (!busy) {
            View load = findViewById(R.id.model_load_main);
            if (load != null) load.setEnabled(!modelLoaded);
        }
    }

    private InferenceControlClient controlClient(ModelTransport transport, String endpoint) {
        if (transport == ModelTransport.LOCAL) return localRuntime().getControlClient();
        if (transport == ModelTransport.USB) {
            return new RemoteInferenceControlClient(ModelEndpoint.USB_BASE, ensureAoaUsbProbe(), 800, 2_000);
        }
        String normalized = ModelEndpoint.INSTANCE.normalizeEthernetBase(endpoint);
        return new RemoteInferenceControlClient(normalized, null, 800, 2_000);
    }

    private void inferCurrentFrame() {
        if (modelMaintenanceInFlight) {
            showBanner(getString(R.string.model_maintenance_wait));
            return;
        }
        if (inferenceInFlight) return;
        if (continuousChunkEnabled && activeRelativeMoveRunnable != null) {
            showBanner(getString(R.string.uavflow_action_incomplete, executedPrefix));
            return;
        }
        if (continuousChunkEnabled && !controlArmed) {
            showBanner(getString(R.string.uavflow_enable_control_before_execution, executedPrefix));
            appendLog("MODEL UAVFlow H" + executedPrefix + " inference blocked: VLN control is off");
            return;
        }
        String readiness = canInferIssue();
        if (readiness != null) {
            showBanner(readiness);
            appendLog("MODEL inference blocked: " + readiness);
            return;
        }
        DJICodecManager codec = codecManager;
        if (continuousChunkEnabled && !chunkExecutionActive) {
            chunkExecutionActive = true;
            chunkStepsExecuted = 0;
            chunkRemaining = 0;
            pendingPolicyActions.clear();
            resetChunkTrajectoryReference();
            chunkPlannedHeadingDegrees = Double.isFinite(aircraftSnapshot.getHeading())
                    ? aircraftSnapshot.getHeading() : 0.0;
        appendLog(getString(R.string.uavflow_inference_started_log, executedPrefix));
        }
        inferenceInFlight = true;
        inferenceStartedAtElapsedMs = SystemClock.elapsedRealtime();
        commandText.setText(R.string.vln_thinking);
        final ModelTransport selectedTransport = modelTransport;
        final String endpoint = ethernetEndpoint.getText().toString().trim();
        final String prompt = normalizedPrompt();
        modelResultText.setText(hilVirtualFramesEnabled
                ? R.string.model_reading_ue_frame : R.string.model_capturing_dji_frame);
        if (hilVirtualFramesEnabled) {
            modelExecutor.execute(() -> {
                Bitmap bitmap = hilController == null ? null : hilController.decodeLatestFrame(2_000L);
                if (bitmap == null) {
                    inferenceFrameCaptureFailed(getString(R.string.ue_frame_decode_failed_or_stale));
                    return;
                }
                runInferenceOnBitmap(bitmap, selectedTransport, endpoint, prompt);
            });
            return;
        }
        if (codec == null) {
            inferenceFrameCaptureFailed(getString(R.string.dji_decoder_not_ready));
            return;
        }
        codec.getBitmap(bitmap -> {
            if (bitmap == null) {
                inferenceFrameCaptureFailed(getString(R.string.dji_camera_frame_capture_failed));
                return;
            }
            modelExecutor.execute(() -> runInferenceOnBitmap(bitmap, selectedTransport, endpoint, prompt));
        });
    }

    private void inferenceFrameCaptureFailed(String message) {
        inferenceInFlight = false;
        runOnUiThread(() -> {
            commandText.setText(R.string.vln_idle_command);
            modelResultText.setText(message);
            appendLog("MODEL " + message);
        });
    }

    private void runInferenceOnBitmap(Bitmap bitmap, ModelTransport selectedTransport,
                                      String endpoint, String prompt) {
        Bitmap modelInputBitmap = bitmap;
        try {
            modelInputBitmap = prepareModelInputBitmap(bitmap);
            InferenceControlClient.Result result;
            if (selectedTransport == ModelTransport.LOCAL) {
                result = localRuntime().infer(
                        modelInputBitmap, prompt, aircraftSnapshot, velocityEstimateMode, executedPrefix);
            } else {
                String base = selectedTransport == ModelTransport.USB
                        ? ModelEndpoint.USB_BASE
                        : ModelEndpoint.INSTANCE.normalizeEthernetBase(endpoint);
                result = new Mini2RemoteInferenceClient(base,
                        selectedTransport == ModelTransport.USB ? ensureAoaUsbProbe() : null)
                        .infer(modelInputBitmap, prompt);
            }
            showModelResult("infer", result);
        } catch (Throwable error) {
            showModelFailure(getString(R.string.model_failure_inference), error, true);
        } finally {
            inferenceInFlight = false;
            try {
                queueInferenceFrameSave(modelInputBitmap, prompt);
            } finally {
                if (modelInputBitmap != bitmap) modelInputBitmap.recycle();
                bitmap.recycle();
            }
        }
    }

    private Bitmap prepareModelInputBitmap(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        Bitmap prepared = sourceWidth == MODEL_FRAME_WIDTH && sourceHeight == MODEL_FRAME_HEIGHT
                ? source
                : Bitmap.createScaledBitmap(source, MODEL_FRAME_WIDTH, MODEL_FRAME_HEIGHT, true);
        if (sourceWidth != lastInferenceSourceWidth || sourceHeight != lastInferenceSourceHeight) {
            lastInferenceSourceWidth = sourceWidth;
            lastInferenceSourceHeight = sourceHeight;
            appendLog(String.format(Locale.US,
                    "MODEL image scale_x %dx%d -> %dx%d -> Qwen grid 256x256",
                    sourceWidth, sourceHeight, MODEL_FRAME_WIDTH, MODEL_FRAME_HEIGHT));
        }
        return prepared;
    }

    private void captureIntermediateObservationThen(Runnable continuation) {
        // 7071579 is current-only: one fixed episode Ref plus the fresh frame
        // at the next replan. Sub-action endpoint images are not model inputs.
        mainHandler.postDelayed(continuation, 120L);
    }

    private String normalizedPrompt() {
        String prompt = modelPrompt.getText().toString().trim();
        return prompt.isEmpty() ? promptPresets.get(0)[1] : prompt;
    }

    private void showModelResult(String operation, InferenceControlClient.Result result) {
        Log.i(TAG, "model " + operation + " status=" + result.getStatusCode() + " ok=" + result.getOk());
        runOnUiThread(() -> {
            String state = result.getOk() ? "OK" : "FAIL";
            boolean wasModelLoaded = modelLoaded;
            boolean loading = false;
            if ("load".equals(operation) || "health".equals(operation)) {
                modelLoaded = result.getOk() && responseSaysModelLoaded(result.getMessage());
                loading = result.getOk() && responseSaysModelLoading(result.getMessage());
                Button load = findViewById(R.id.model_load_main);
                load.setText(modelLoaded ? getString(R.string.model_loaded)
                        : loading ? getString(R.string.model_loading) : getString(R.string.action_load));
                load.setEnabled(!modelLoaded && !loading);
                mainHandler.removeCallbacks(modelLoadPollRunnable);
                if (loading && modelLoadPollAttempts < 240) {
                    mainHandler.postDelayed(modelLoadPollRunnable, 500L);
                }
            }
            if ("infer".equals(operation)) {
                latestInferenceLatencyMs = Math.max(0L, SystemClock.elapsedRealtime() - inferenceStartedAtElapsedMs);
                if (!result.getOk()) abortChunkExecution(getString(R.string.inference_failed));
            }
            modelResultText.setText(operation + " · " + state + " · " + result.getStatusCode() + "\n" + result.getMessage());
            renderModelStatus(transportLabel() + " · " + state);
            if ("load".equals(operation) && loading) {
                appendLog(getString(R.string.model_loading_log));
            } else if (("health".equals(operation) || "load".equals(operation)) && modelLoaded && !wasModelLoaded) {
                appendLog(getString(R.string.model_loaded_log, executedPrefix));
            } else if (!"health".equals(operation) && !"infer".equals(operation)) {
                appendLog("MODEL " + operation + " " + state + " status=" + result.getStatusCode());
            }
            if ("infer".equals(operation) && result.getOk()) applyInferenceCommand(result.getMessage());
        });
    }

    private boolean responseSaysModelLoaded(String message) {
        try {
            JSONObject json = new JSONObject(message);
            if (json.optBoolean("model_loaded", false)) return true;
            String state = json.optString("state", "");
            return "ready".equalsIgnoreCase(state) || "loaded".equalsIgnoreCase(state);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean responseSaysModelLoading(String message) {
        try {
            return "loading".equalsIgnoreCase(new JSONObject(message).optString("state", ""));
        } catch (Exception ignored) {
            return false;
        }
    }

    private void showModelFailure(String label, Throwable error, boolean inferenceFailure) {
        Log.e(TAG, label, error);
        runOnUiThread(() -> {
            modelResultText.setText(label + "：" + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()));
            renderModelStatus(transportLabel() + " · FAIL");
            if (inferenceFailure) commandText.setText(R.string.vln_idle_command);
            if (inferenceFailure) abortChunkExecution(label);
            appendLog("MODEL " + label + " · " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()));
        });
    }

    private void applyInferenceCommand(String raw) {
        List<OpenFlyRelativeAction> actions = parseRelativeModelActions(raw);
        if (actions.isEmpty()) {
            commandText.setText(R.string.model_result_missing_action);
            appendLog(getString(R.string.model_output_parse_failed));
            abortChunkExecution(getString(R.string.model_output_parse_failed));
            return;
        }
        pendingPolicyActions.clear();
        if (chunkExecutionActive && continuousChunkEnabled) {
            for (int index = 1; index < Math.min(actions.size(), executedPrefix); index++) {
                pendingPolicyActions.addLast(actions.get(index));
            }
            chunkRemaining = pendingPolicyActions.size();
        } else {
            chunkRemaining = 0;
        }
        executePolicyAction(actions.get(0));
    }

    private void executeNextQueuedPolicyAction() {
        if (!chunkExecutionActive || !continuousChunkEnabled || !controlArmed) return;
        OpenFlyRelativeAction next = pendingPolicyActions.pollFirst();
        chunkRemaining = pendingPolicyActions.size();
        if (next == null) {
            finishRelativeMove(true, getString(R.string.uavflow_h_completed, executedPrefix));
            return;
        }
        executePolicyAction(next);
    }

    private void executePolicyAction(OpenFlyRelativeAction mapped) {
        double forwardMeters = mapped.getForwardMeters();
        double rightMeters = mapped.getRightMeters();
        double upMeters = mapped.getUpMeters();
        double stopScore = mapped.getStopScore();
        double yawDeltaDegrees = mapped.getYawDegrees();
        renderRelativeAction(forwardMeters, rightMeters, upMeters, stopScore, yawDeltaDegrees);
        appendLog(getString(R.string.model_output_action_summary,
                forwardMeters, rightMeters, upMeters, yawDeltaDegrees, stopScore,
                latestInferenceLatencyMs == null ? 0L : latestInferenceLatencyMs));

        if (chunkExecutionActive) {
            chunkStepsExecuted++;
            appendLog(getString(R.string.uavflow_chunk_progress,
                    executedPrefix, chunkStepsExecuted, executedPrefix, chunkRemaining));
        }
        if (mapped.shouldStop(stopThreshold)) {
            chunkExecutionActive = false;
            chunkRemaining = 0;
            pendingPolicyActions.clear();
            normalStop(getString(R.string.model_stop_reason, stopScore, stopThreshold));
            renderRelativeAction(forwardMeters, rightMeters, upMeters, stopScore, yawDeltaDegrees);
            return;
        }
        if (Math.hypot(forwardMeters, rightMeters) > 10.0 || Math.abs(upMeters) > 0.5) {
            appendLog(getString(R.string.safety_action_out_of_range));
            abortChunkExecution(getString(R.string.model_relative_position_out_of_range));
            return;
        }
        String descentIssue = descentSafetyIssue(upMeters);
        if (descentIssue != null) {
            appendLog(getString(R.string.safety_reason_log, descentIssue));
            abortChunkExecution(getString(R.string.model_descent_target_unsafe));
            return;
        }
        String issue = coreSafetyIssue();
        if (issue == null && (!controlArmed || !aircraftSnapshot.getVirtualStickEnabled())) issue = "preview only; VLN control is off";
        if (issue != null) {
            appendLog(getString(R.string.safety_reason_log, issue));
            safetyGateText.setText(getString(R.string.safe_gate_blocked, issue));
            return;
        }
        appendLog(getString(R.string.position_action_started,
                getString(velocityEstimateMode ? R.string.velocity_estimate : R.string.gps_loop)));
        double referenceHeadingDegrees = Double.isFinite(aircraftSnapshot.getHeading())
                ? aircraftSnapshot.getHeading() : 0.0;
        if (chunkExecutionActive) {
            if (!Double.isFinite(chunkPlannedHeadingDegrees)) {
                chunkPlannedHeadingDegrees = referenceHeadingDegrees;
            }
            referenceHeadingDegrees = chunkPlannedHeadingDegrees;
            chunkPlannedHeadingDegrees = OrinTrajectorySemantics.wrapDegrees(
                    referenceHeadingDegrees + yawDeltaDegrees);
        }
        if (velocityEstimateMode) {
            startTimedRelativeMove(
                    forwardMeters, rightMeters, upMeters, true, referenceHeadingDegrees, yawDeltaDegrees);
        } else {
            startGpsRelativeMove(
                    forwardMeters, rightMeters, upMeters, true, referenceHeadingDegrees, yawDeltaDegrees);
        }
    }

    private List<OpenFlyRelativeAction> parseRelativeModelActions(String raw) {
        List<OpenFlyRelativeAction> parsed = new ArrayList<>();
        JSONObject response = parseResponseObject(raw);
        if (response == null) return parsed;
        boolean explicitYaw = response.optBoolean("yaw_is_explicit", false);
        JSONArray actions = response.optJSONArray("actions");
        if (actions != null) {
            boolean nestedRows = actions.optJSONArray(0) != null;
            int rows = nestedRows ? actions.length() : 1;
            for (int index = 0; index < rows; index++) {
                JSONArray row = nestedRows ? actions.optJSONArray(index) : actions;
                if (row == null) continue;
                OpenFlyRelativeAction action = parseModelActionRow(row, explicitYaw);
                if (action == null) {
                    parsed.clear();
                    break;
                }
                parsed.add(action);
            }
            if (!parsed.isEmpty()) return parsed;
        }
        JSONObject command = findCommandObject(raw);
        if (command == null) return parsed;
        double forward = command.optDouble("target_forward_m", Double.NaN);
        double right = command.optDouble("target_right_m", Double.NaN);
        double up = command.optDouble("target_up_m", Double.NaN);
        double yaw = command.optDouble("yaw_delta_deg", 0.0);
        double stop = command.optDouble("stop", command.optBoolean("stop_requested", false) ? 1.0 : 0.0);
        if (!Double.isFinite(forward) || !Double.isFinite(right) || !Double.isFinite(up)
                || !Double.isFinite(yaw) || !Double.isFinite(stop)) return parsed;
        parsed.add(explicitYaw
                ? OpenFlyActionSemantics.mapUavFlow(forward, right, up, yaw, stop)
                : legacyAction(forward, -right, up, stop));
        return parsed;
    }

    private OpenFlyRelativeAction parseModelActionRow(JSONArray row, boolean explicitYaw) {
        if (explicitYaw) {
            if (row.length() < 5) return null;
            double[] values = {row.optDouble(0, Double.NaN), row.optDouble(1, Double.NaN),
                    row.optDouble(2, Double.NaN), row.optDouble(3, Double.NaN),
                    row.optDouble(4, Double.NaN)};
            for (double value : values) if (!Double.isFinite(value)) return null;
            return OpenFlyActionSemantics.mapUavFlow(values[0], values[1], values[2], values[3], values[4]);
        }
        if (row.length() < 4) return null;
        double[] values = {row.optDouble(0, Double.NaN), row.optDouble(1, Double.NaN),
                row.optDouble(2, Double.NaN), row.optDouble(3, Double.NaN)};
        for (double value : values) if (!Double.isFinite(value)) return null;
        return legacyAction(values[0], values[1], values[2], values[3]);
    }

    private OpenFlyRelativeAction legacyAction(double x, double y, double z, double stop) {
        OpenFlyRelativeAction legacy = OpenFlyActionSemantics.map(x, y, z, stop);
        return new OpenFlyRelativeAction(
                legacy.getForwardMeters(), legacy.getRightMeters(), legacy.getUpMeters(),
                OrinTrajectorySemantics.yawDeltaDegrees(
                        legacy.getForwardMeters(), legacy.getRightMeters()),
                legacy.getStopScore());
    }

    private void renderRelativeAction(double x, double y, double z, double stop, double yawDeltaDegrees) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        appendColored(text, latestInferenceLatencyMs == null ? "IDLE  " : latestInferenceLatencyMs + "ms  ", 0xFFFFC45C);
        appendColored(text, String.format(Locale.US, "X %.2f  ", x), 0xFF42C8FF);
        appendColored(text, String.format(Locale.US, "Y %.2f  ", y), 0xFF58E6A9);
        appendColored(text, String.format(Locale.US, "Z %.2f  ", z), 0xFFFFD166);
        appendColored(text, String.format(Locale.US, "YAWΔ %.1f°  ", yawDeltaDegrees), 0xFF80CBC4);
        appendColored(text, String.format(Locale.US, "STOP %.2f", stop), stop >= stopThreshold ? 0xFFFF5A66 : 0xFFC5A3FF);
        commandText.setText(text);
    }

    private void appendColored(SpannableStringBuilder target, String value, int color) {
        int start = target.length();
        target.append(value);
        target.setSpan(new ForegroundColorSpan(color), start, target.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private JSONObject parseResponseObject(String raw) {
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            return start >= 0 && end > start ? new JSONObject(raw.substring(start, end + 1)) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void abortChunkExecution(String reason) {
        if (!chunkExecutionActive && chunkRemaining == 0 && pendingPolicyActions.isEmpty()) {
            resetChunkTrajectoryReference();
            return;
        }
        chunkExecutionActive = false;
        chunkRemaining = 0;
        chunkStepsExecuted = 0;
        pendingPolicyActions.clear();
        lastYawRateDegreesPerSecond = 0.0;
        resetChunkTrajectoryReference();
        if (autoInferenceEnabled && continuousChunkEnabled) {
            autoInferenceEnabled = false;
            mainHandler.removeCallbacks(autoInferenceRunnable);
            ((Button) findViewById(R.id.auto_infer_button)).setText(getString(R.string.vln_auto_off));
            appendLog(getString(R.string.uavflow_auto_stopped, executedPrefix, reason));
        }
        if (aircraftBridge != null) aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
        appendLog(getString(R.string.uavflow_stopped, executedPrefix, reason));
    }

    private void resetChunkTrajectoryReference() {
        chunkPlannedHeadingDegrees = Double.NaN;
        flyThroughCarryNorthMeters = 0.0;
        flyThroughCarryEastMeters = 0.0;
        flyThroughCarryUpMeters = 0.0;
    }

    private JSONObject findCommandObject(String raw) {
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start < 0 || end <= start) return null;
            return findCommandObject(new JSONObject(raw.substring(start, end + 1)), 0);
        } catch (Exception error) {
            appendLog("MODEL command parse failed: " + error.getMessage());
            return null;
        }
    }

    private JSONObject findCommandObject(JSONObject object, int depth) {
        if (object.has("vx") && object.has("vy") && object.has("vz")) return object;
        if (depth >= 4) return null;
        String[] keys = new String[] {"command", "action", "result", "data", "output", "body"};
        for (String key : keys) {
            Object value = object.opt(key);
            if (value instanceof JSONObject) {
                JSONObject nested = findCommandObject((JSONObject) value, depth + 1);
                if (nested != null) return nested;
            } else if (value instanceof String) {
                JSONObject nested = findCommandObject((String) value);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private void renderModelStatus(String value) {
        modelStatusText.setText(value);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        forwardAccessoryIntent(intent);
        if (mainUiInitialized) handleDebugControlIntent(intent);
    }

    private void forwardAccessoryIntent(Intent intent) {
        if (intent != null && UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
            Intent attached = new Intent(DJISDKManager.USB_ACCESSORY_ATTACHED);
            sendBroadcast(attached);
            Log.i(TAG, "forwarded DJI USB accessory attach");
        }
    }

    private void handleDebugControlIntent(Intent intent) {
        if (intent != null && ACTION_MODEL_HEALTH.equals(intent.getAction())) {
            runModelOperation("health");
        } else if (intent != null && ACTION_MODEL_SMOKE.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            runDebugModelSmoke(intent.getStringExtra("prompt"));
        } else if (intent != null && ACTION_MOCK_UI_STATE.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            applyMockUiState(intent.getStringExtra("mode"));
        } else if (intent != null && ACTION_SURVEY_REGRESSION.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            generateDebugSurveyRegression(intent);
        } else if (intent != null && ACTION_SURVEY_UI_DRY_RUN_CONTROL.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            handleSurveyUiDryRunControl(intent.getStringExtra("command"));
        } else if (intent != null && ACTION_HIL_SIMULATOR_REGRESSION.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            startHilSimulatorRegression();
        } else if (intent != null
                && ACTION_HIL_RECONNECT_SIMULATOR_REGRESSION.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            reconnectProductAndStartHilSimulatorRegression();
        } else if (intent != null && ACTION_HIL_OFFLINE_REGRESSION.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            startHilOfflineRegression(intent.getStringExtra("host"));
        } else if (intent != null && ACTION_HIL_RESET_AND_TAKEOFF.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            resetHilSimulatorSessionAndTakeoff();
        } else if (intent != null && ACTION_HIL_AXIS_CALIBRATION.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            startHilAxisCalibration();
        } else if (intent != null && ACTION_CAMERA_CADENCE_TEST.equals(intent.getAction())
                && (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            startCameraCadenceTest(intent.getIntExtra("shots", 6),
                    intent.getStringExtra("periods_ms"));
        }
    }

    private void startCameraCadenceTest(int requestedShotsPerStage, String requestedPeriodsMs) {
        if (cameraCadenceTestActive) {
            appendLog("CAMERA_CADENCE blocked: test already active");
            return;
        }
        if (aircraftBridge == null || !aircraftSnapshot.getConnected()) {
            appendLog("CAMERA_CADENCE RESULT=BLOCKED reason=camera_not_connected");
            return;
        }
        if (aircraftSnapshot.getFlying() || aircraftSnapshot.getSimulatorFlying()) {
            appendLog("CAMERA_CADENCE RESULT=BLOCKED reason=aircraft_must_be_grounded");
            return;
        }
        if (aircraftSnapshot.getRecording()) {
            appendLog("CAMERA_CADENCE RESULT=BLOCKED reason=video_recording_active");
            return;
        }
        if (isSurveySimulatorExecutionActive() || controlArmed || autoInferenceEnabled) {
            appendLog("CAMERA_CADENCE RESULT=BLOCKED reason=control_or_survey_active");
            return;
        }
        cameraCadenceShotsPerStage = Math.max(3, Math.min(20, requestedShotsPerStage));
        cameraCadenceTestPeriodsMs = parseCameraCadencePeriods(requestedPeriodsMs);
        cameraCadenceTestActive = true;
        cameraCadenceTestGeneration++;
        cameraCadenceStageIndex = 0;
        appendLog("CAMERA_CADENCE START periods_ms=" + formatCameraCadencePeriods()
                + " shots_per_stage="
                + cameraCadenceShotsPerStage + " mode=current_production_takePhoto grounded=true");
        runCameraCadenceBaseline(cameraCadenceTestGeneration);
    }

    private long[] parseCameraCadencePeriods(String requestedPeriodsMs) {
        if (requestedPeriodsMs == null || requestedPeriodsMs.trim().isEmpty()) {
            return CAMERA_CADENCE_TEST_PERIODS_MS.clone();
        }
        ArrayList<Long> periods = new ArrayList<>();
        for (String value : requestedPeriodsMs.split(",")) {
            try {
                long period = Long.parseLong(value.trim());
                if (period >= 500L && period <= 10_000L) periods.add(period);
            } catch (NumberFormatException ignored) {
            }
        }
        if (periods.isEmpty()) return CAMERA_CADENCE_TEST_PERIODS_MS.clone();
        long[] result = new long[periods.size()];
        for (int index = 0; index < periods.size(); index++) result[index] = periods.get(index);
        return result;
    }

    private String formatCameraCadencePeriods() {
        StringBuilder value = new StringBuilder();
        for (long period : cameraCadenceTestPeriodsMs) {
            if (value.length() > 0) value.append(',');
            value.append(period);
        }
        return value.toString();
    }

    private void runCameraCadenceBaseline(int generation) {
        if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration) return;
        cameraCadenceShotInFlight = true;
        long requestUptimeMs = SystemClock.uptimeMillis();
        appendLog("CAMERA_CADENCE BASELINE requested");
        cameraCadenceShotTimeoutRunnable = () -> {
            if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration
                    || !cameraCadenceShotInFlight) return;
            cameraCadenceShotInFlight = false;
            cancelCameraCadenceTest("baseline_photo_timeout");
        };
        mainHandler.postDelayed(cameraCadenceShotTimeoutRunnable, 8_000L);
        aircraftBridge.takePhoto((ok, message) -> {
            if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration
                    || !cameraCadenceShotInFlight) return;
            mainHandler.removeCallbacks(cameraCadenceShotTimeoutRunnable);
            cameraCadenceShotInFlight = false;
            long latencyMs = Math.max(0L, SystemClock.uptimeMillis() - requestUptimeMs);
            appendLog("CAMERA_CADENCE BASELINE result=" + (ok ? "OK" : "FAIL")
                    + " latency_ms=" + latencyMs + " detail=" + message);
            if (!ok) {
                cancelCameraCadenceTest("baseline_photo_failed:" + message);
                return;
            }
            mainHandler.postDelayed(() -> startCameraCadenceStage(generation), 1_500L);
        });
    }

    private void startCameraCadenceStage(int generation) {
        if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration) return;
        if (cameraCadenceStageIndex >= cameraCadenceTestPeriodsMs.length) {
            cameraCadenceTestActive = false;
            appendLog("CAMERA_CADENCE COMPLETE");
                    showBanner(getString(R.string.camera_rate_test_completed));
            return;
        }
        cameraCadenceStageShotIndex = 0;
        cameraCadenceStageOk = 0;
        cameraCadenceStageFail = 0;
        cameraCadenceStageTimeout = 0;
        cameraCadenceStageLatencyMs = 0L;
        cameraCadenceStageFirstRequestUptimeMs = 0L;
        cameraCadenceStageLastRequestUptimeMs = 0L;
        cameraCadenceShotInFlight = false;
        cameraCadenceNextRequestUptimeMs = SystemClock.uptimeMillis() + 1_000L;
        appendLog("CAMERA_CADENCE STAGE period_ms="
                + cameraCadenceTestPeriodsMs[cameraCadenceStageIndex]);
        mainHandler.removeCallbacks(cameraCadenceTickRunnable);
        mainHandler.postAtTime(cameraCadenceTickRunnable, cameraCadenceNextRequestUptimeMs);
    }

    private void runCameraCadenceTestTick() {
        if (!cameraCadenceTestActive || cameraCadenceShotInFlight) return;
        int generation = cameraCadenceTestGeneration;
        if (aircraftSnapshot.getFlying() || aircraftSnapshot.getSimulatorFlying()
                || isSurveySimulatorExecutionActive() || controlArmed) {
            cancelCameraCadenceTest("safety_state_changed");
            return;
        }
        if (cameraCadenceStageShotIndex >= cameraCadenceShotsPerStage) {
            finishCameraCadenceStage(generation);
            return;
        }
        long requestUptimeMs = SystemClock.uptimeMillis();
        if (cameraCadenceStageFirstRequestUptimeMs == 0L) {
            cameraCadenceStageFirstRequestUptimeMs = requestUptimeMs;
        }
        cameraCadenceStageLastRequestUptimeMs = requestUptimeMs;
        cameraCadenceStageShotIndex++;
        cameraCadenceShotInFlight = true;
        long periodMs = cameraCadenceTestPeriodsMs[cameraCadenceStageIndex];
        cameraCadenceNextRequestUptimeMs += periodMs;
        int stage = cameraCadenceStageIndex;
        int shot = cameraCadenceStageShotIndex;
        cameraCadenceShotTimeoutRunnable = () -> {
            if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration
                    || !cameraCadenceShotInFlight) return;
            cameraCadenceShotInFlight = false;
            cameraCadenceStageTimeout++;
            cameraCadenceStageLatencyMs += 8_000L;
            appendLog("CAMERA_CADENCE SHOT period_ms=" + periodMs + " shot=" + shot
                    + " result=TIMEOUT latency_ms=8000");
            scheduleNextCameraCadenceShot(generation);
        };
        mainHandler.postDelayed(cameraCadenceShotTimeoutRunnable, 8_000L);
        aircraftBridge.takePhoto((ok, message) -> {
            if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration
                    || stage != cameraCadenceStageIndex || !cameraCadenceShotInFlight) return;
            mainHandler.removeCallbacks(cameraCadenceShotTimeoutRunnable);
            cameraCadenceShotInFlight = false;
            long latencyMs = Math.max(0L, SystemClock.uptimeMillis() - requestUptimeMs);
            cameraCadenceStageLatencyMs += latencyMs;
            if (ok) cameraCadenceStageOk++;
            else cameraCadenceStageFail++;
            appendLog("CAMERA_CADENCE SHOT period_ms=" + periodMs + " shot=" + shot
                    + " result=" + (ok ? "OK" : "FAIL") + " latency_ms=" + latencyMs
                    + " detail=" + message);
            scheduleNextCameraCadenceShot(generation);
        });
    }

    private void scheduleNextCameraCadenceShot(int generation) {
        if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration) return;
        if (cameraCadenceStageShotIndex >= cameraCadenceShotsPerStage) {
            finishCameraCadenceStage(generation);
            return;
        }
        long now = SystemClock.uptimeMillis();
        mainHandler.removeCallbacks(cameraCadenceTickRunnable);
        mainHandler.postAtTime(cameraCadenceTickRunnable, Math.max(now, cameraCadenceNextRequestUptimeMs));
    }

    private void finishCameraCadenceStage(int generation) {
        if (!cameraCadenceTestActive || generation != cameraCadenceTestGeneration) return;
        int total = cameraCadenceStageOk + cameraCadenceStageFail + cameraCadenceStageTimeout;
        long spanMs = cameraCadenceStageShotIndex <= 1 ? 0L
                : cameraCadenceStageLastRequestUptimeMs - cameraCadenceStageFirstRequestUptimeMs;
        double observedPeriodMs = cameraCadenceStageShotIndex <= 1 ? 0.0
                : spanMs / (double) (cameraCadenceStageShotIndex - 1);
        double averageLatencyMs = total == 0 ? 0.0 : cameraCadenceStageLatencyMs / (double) total;
        appendLog(String.format(Locale.US,
                "CAMERA_CADENCE RESULT period_ms=%d observed_period_ms=%.0f shots=%d ok=%d fail=%d timeout=%d avg_latency_ms=%.0f",
                cameraCadenceTestPeriodsMs[cameraCadenceStageIndex], observedPeriodMs,
                total, cameraCadenceStageOk, cameraCadenceStageFail,
                cameraCadenceStageTimeout, averageLatencyMs));
        cameraCadenceStageIndex++;
        mainHandler.postDelayed(() -> startCameraCadenceStage(generation), 1_500L);
    }

    private void cancelCameraCadenceTest(String reason) {
        if (!cameraCadenceTestActive) return;
        cameraCadenceTestActive = false;
        cameraCadenceTestGeneration++;
        cameraCadenceShotInFlight = false;
        mainHandler.removeCallbacks(cameraCadenceTickRunnable);
        if (cameraCadenceShotTimeoutRunnable != null) {
            mainHandler.removeCallbacks(cameraCadenceShotTimeoutRunnable);
        }
        appendLog("CAMERA_CADENCE RESULT=ABORTED reason=" + reason);
    }

    private void startHilAxisCalibration() {
        if (hilAxisCalibrationActive) {
            appendLog("HIL_AXIS calibration already active");
            return;
        }
        if (aircraftBridge == null || !aircraftBridge.isSimulatorRegressionReady()
                || !aircraftSnapshot.getSimulatorFlying()) {
            appendLog("HIL_AXIS RESULT=FAIL simulator is not flying with fresh RAW state");
            return;
        }
        hilAxisCalibrationActive = true;
        int generation = ++hilAxisCalibrationGeneration;
        appendLog("HIL_AXIS START source=DJI SimulatorState RAW");
        aircraftBridge.enableSimulatorRegressionVirtualStick((ok, message) -> {
            if (!hilAxisCalibrationActive || generation != hilAxisCalibrationGeneration) return;
            appendLog("HIL_AXIS VS ok=" + ok + " message=" + message);
            if (!ok) {
                finishHilAxisCalibration(generation, false, "VS enable failed");
                return;
            }
            runHilAxisPulse(generation, 0, new String[] {"FORWARD", "BACKWARD", "RIGHT", "LEFT"});
        });
    }

    private void runHilAxisPulse(int generation, int index, String[] labels) {
        if (!hilAxisCalibrationActive || generation != hilAxisCalibrationGeneration) return;
        if (index >= labels.length) {
            finishHilAxisCalibration(generation, true, "four pulses completed");
            return;
        }
        Mini2AircraftBridge.SimulatorSample start = aircraftBridge.currentSimulatorSample();
        if (start == null) {
            finishHilAxisCalibration(generation, false, "RAW sample missing");
            return;
        }
        String label = labels[index];
        float forward = "FORWARD".equals(label) ? 0.5f : "BACKWARD".equals(label) ? -0.5f : 0f;
        float right = "RIGHT".equals(label) ? 0.5f : "LEFT".equals(label) ? -0.5f : 0f;
        appendLog(formatHilAxisSample("BEGIN_" + label, start));
        if (!aircraftBridge.sendSimulatorRegressionVelocity(forward, right, 0f, 0f)) {
            finishHilAxisCalibration(generation, false, label + " command blocked");
            return;
        }
        mainHandler.postDelayed(() -> {
            if (!hilAxisCalibrationActive || generation != hilAxisCalibrationGeneration) return;
            aircraftBridge.sendSimulatorRegressionVelocity(0f, 0f, 0f, 0f);
            mainHandler.postDelayed(() -> {
                if (!hilAxisCalibrationActive || generation != hilAxisCalibrationGeneration) return;
                Mini2AircraftBridge.SimulatorSample end = aircraftBridge.currentSimulatorSample();
                if (end == null) {
                    finishHilAxisCalibration(generation, false, label + " end sample missing");
                    return;
                }
                double east = end.getEastMeters() - start.getEastMeters();
                double north = end.getNorthMeters() - start.getNorthMeters();
                double heading = Math.toRadians(start.getYawDegrees());
                double bodyForward = north * Math.cos(heading) + east * Math.sin(heading);
                double bodyRight = east * Math.cos(heading) - north * Math.sin(heading);
                appendLog(String.format(Locale.US,
                        "HIL_AXIS %s dSdkX=%.3f dSdkY=%.3f bodyForward=%.3f bodyRight=%.3f "
                                + "dRoll=%.2f dPitch=%.2f yaw=%.2f",
                        label, east, north, bodyForward, bodyRight,
                        end.getRollDegrees() - start.getRollDegrees(),
                        end.getPitchDegrees() - start.getPitchDegrees(), start.getYawDegrees()));
                runHilAxisPulse(generation, index + 1, labels);
            }, 900L);
        }, 800L);
    }

    private String formatHilAxisSample(String label, Mini2AircraftBridge.SimulatorSample sample) {
        return String.format(Locale.US,
                "HIL_AXIS %s x=%.3f y=%.3f z=%.3f roll=%.2f pitch=%.2f yaw=%.2f",
                label, sample.getEastMeters(), sample.getNorthMeters(), -sample.getDownMeters(),
                sample.getRollDegrees(), sample.getPitchDegrees(), sample.getYawDegrees());
    }

    private void finishHilAxisCalibration(int generation, boolean passed, String reason) {
        if (generation != hilAxisCalibrationGeneration) return;
        hilAxisCalibrationActive = false;
        if (aircraftBridge != null) {
            aircraftBridge.sendSimulatorRegressionVelocity(0f, 0f, 0f, 0f);
            aircraftBridge.disableSimulatorRegressionVirtualStick((ok, message) ->
                    appendLog("HIL_AXIS VS_RELEASE ok=" + ok + " message=" + message));
        }
        appendLog("HIL_AXIS RESULT=" + (passed ? "PASS" : "FAIL") + " reason=" + reason);
    }

    private void resetHilSimulatorSessionAndTakeoff() {
        appendLog("HIL RECOVERY START stopLink=true resetSimulator=true autoTakeoff=true");
        hilUiSimulatorTakeoffPending = false;
        hilUiSimulatorTakeoffGeneration += 1;
        if (hilController != null && hilController.isRunning()) {
            stopHilLink(getString(R.string.hil_auto_recovery), false);
        }
        Runnable startFreshSession = () -> {
            if (aircraftBridge == null || !aircraftSnapshot.getConnected()) {
                appendLog("HIL RECOVERY FAIL aircraft unavailable");
                return;
            }
            hilSimulatorCleanStartRequired = false;
            aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
            appendLog("HIL RECOVERY starting fresh DJI Simulator session");
            aircraftBridge.setSimulatorEnabled(true, (ok, message) -> {
                appendLog("HIL RECOVERY simulatorStart ok=" + ok + " message=" + message
                        + " active=" + aircraftBridge.isSimulatorActuallyActive());
                if (!ok || !aircraftBridge.isSimulatorActuallyActive()) {
                    appendLog("HIL RECOVERY FAIL Simulator did not activate");
                    return;
                }
                hilStartedSimulator = true;
                if (hilController == null || !hilController.isRunning()) toggleHilLink();
                mainHandler.postDelayed(this::startHilUiSimulatorTakeoff, 500L);
            });
        };
        if (aircraftBridge == null) {
            appendLog("HIL RECOVERY FAIL aircraft bridge unavailable");
            return;
        }
        if (aircraftBridge.isSimulatorActuallyActive()) {
            appendLog("HIL RECOVERY stopping residual DJI Simulator session");
            aircraftBridge.setSimulatorEnabled(false, (ok, message) -> {
                appendLog("HIL RECOVERY simulatorStop ok=" + ok + " message=" + message
                        + " active=" + aircraftBridge.isSimulatorActuallyActive());
                if (!ok || aircraftBridge.isSimulatorActuallyActive()) {
                    appendLog("HIL RECOVERY FAIL residual Simulator did not stop");
                    return;
                }
                mainHandler.postDelayed(startFreshSession, 1_000L);
            });
        } else {
            mainHandler.postDelayed(startFreshSession, 500L);
        }
    }

    private void reconnectProductAndStartHilSimulatorRegression() {
        appendLog("HIL_REGRESSION PRODUCT_RECONNECT stopping DJI product session");
        DJISDKManager.getInstance().stopConnectionToProduct();
        mainHandler.postDelayed(() -> {
            boolean started = DJISDKManager.getInstance().startConnectionToProduct();
            appendLog("HIL_REGRESSION PRODUCT_RECONNECT start requested=" + started);
            startHilSimulatorRegression();
        }, 1_500L);
    }

    private void startHilOfflineRegression(String requestedHost) {
        if (hilOfflineRegressionRunner != null) {
            hilOfflineRegressionRunner.close();
            hilOfflineRegressionRunner = null;
        }
        String host = requestedHost == null || requestedHost.trim().isEmpty()
                ? "127.0.0.1" : requestedHost.trim();
        appendLog("HIL_OFFLINE requested host=" + host
                + " source=MOCK_RC+MOCK_DYNAMICS aircraft=NOT_REQUIRED");
        HilOfflineRegressionRunner runner = new HilOfflineRegressionRunner(
                new HilOfflineRegressionRunner.Listener() {
                    @Override public void onLog(String message) {
                        mainHandler.post(() -> appendLog(message));
                    }

                    @Override public void onFinished(HilOfflineRegressionRunner.Result result) {
                        mainHandler.post(() -> {
                            appendLog(String.format(Locale.US,
                                    "HIL_OFFLINE RESULT=%s source=MOCK sent=%d received=%d rate=%.1fHz "
                                            + "alt=%.2fm north=%.2fm brakePitch=%.1fdeg peer=%s reason=%s",
                                    result.getPassed() ? "PASS" : "FAIL", result.getSentPoseCount(),
                                    result.getReceivedPoseCount(), result.getMeasuredPoseHz(), result.getAltitudeMeters(),
                                    result.getNorthMeters(), result.getMaxBrakePitchDegrees(),
                                    result.getPeerFresh(), result.getReason()));
                            hilOfflineRegressionRunner = null;
                        });
                    }
                });
        hilOfflineRegressionRunner = runner;
        runner.start(host);
    }

    private void startHilSimulatorRegression() {
        if (hilSimulatorRegressionCleanupActive) {
            appendLog("HIL_REGRESSION request ignored while prior cleanup is active");
            return;
        }
        if (hilSimulatorRegressionActive) {
            finishHilSimulatorRegression(false, getString(R.string.new_regression_aborted_previous));
        }
        hilSimulatorRegressionActive = true;
        hilSimulatorRegressionStage = 0;
        hilSimulatorRegressionStartedMs = SystemClock.elapsedRealtime();
        hilSimulatorRegressionStageStartedMs = hilSimulatorRegressionStartedMs;
        hilSimulatorRegressionLastWaitLogMs = 0L;
        int generation = ++hilSimulatorRegressionGeneration;
        hilSimulatorRegressionOrigin = null;
        hilSimulatorRegressionBaseline = null;
        hilSimulatorRegressionTakeoffResult = "not-run";
        hilSimulatorRegressionSawMotors = false;
        hilSimulatorRegressionSawFlying = false;
        hilSimulatorRegressionMaxRawHz = 0.0;
        hilSimulatorRegressionMaxAltitudeGain = 0.0;
        hilSimulatorRegressionMaxForwardMovement = 0.0;
        hilSimulatorRegressionMaxForwardAttitude = 0.0;
        hilSimulatorRegressionTakeoffAttempts = 0;
        hilSimulatorRegressionMotorFallback = false;
        hilSimulatorRegressionNavigationReadySinceMs = 0L;
        hilSimulatorRegressionLandingConfirmationRequested = false;
        hilSimulatorRegressionLastLandingLogMs = 0L;
        hilSimulatorRegressionCompletedCycles = 0;
        appendLog("HIL_REGRESSION START source=DJI SimulatorState RAW synthetic=false");
        ensureHilController();
        if (hilController.isRunning()) {
            stopHilLink(getString(R.string.raw_auto_regression_reset_link), false);
        }
        hilSimulatorRegressionLoopbackPeer = new HilLoopbackPeer(30_020);
        if (!hilSimulatorRegressionLoopbackPeer.start()) {
            finishHilSimulatorRegression(false, "loopback UE peer failed to start");
            return;
        }
        hilController.start(new AndroidHilController.Config(
                HilConnectionMode.LAN, "127.0.0.1", 30_020, 30_021, 30_022,
                hilSimulatorStateHz, hilSimulatorStateHz, 1_000L));
        if (!hilController.isRunning()) {
            finishHilSimulatorRegression(false, "loopback HIL controller failed to start");
            return;
        }
        hilSimulatorRegressionOwnsLink = true;
        hilStartedSimulator = false;
        hilSimulatorAutoStartPending = true;
        if (aircraftBridge != null) {
            aircraftBridge.setSimulatorUpdateFrequencyHz(hilSimulatorStateHz);
        }
        ensureHilSimulatorStarted();
        mainHandler.removeCallbacks(hilSimulatorRegressionRunnable);
        mainHandler.post(hilSimulatorRegressionRunnable);
        modelExecutor.execute(() -> {
            try {
                Thread.sleep(32_000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
            mainHandler.post(() -> {
                if (hilSimulatorRegressionActive
                        && generation == hilSimulatorRegressionGeneration) {
                    finishHilSimulatorRegression(false, "independent 32s watchdog timeout");
                }
            });
        });
    }

    private void runHilSimulatorRegressionTick() {
        if (!hilSimulatorRegressionActive) return;
        long now = SystemClock.elapsedRealtime();
        if (now - hilSimulatorRegressionStartedMs > 30_000L) {
            finishHilSimulatorRegression(false, "30s timeout");
            return;
        }
        if (aircraftBridge == null || hilController == null || !hilController.isRunning()) {
            finishHilSimulatorRegression(false, "HIL controller unavailable");
            return;
        }
        Mini2AircraftBridge.SimulatorSample sample = aircraftBridge.currentSimulatorSample();
        boolean rawFresh = sample != null
                && SystemClock.elapsedRealtimeNanos() - sample.getElapsedRealtimeNanos() <= 500_000_000L;
        boolean simulatorRawReady = rawFresh && aircraftBridge.isSimulatorRegressionReady()
                && !hilSimulatorStartInFlight && !hilSimulatorAutoStartPending;
        boolean navigationReady = aircraftBridge.isSimulatorNavigationReady();
        if (simulatorRawReady && navigationReady) {
            if (hilSimulatorRegressionNavigationReadySinceMs == 0L) {
                hilSimulatorRegressionNavigationReadySinceMs = now;
            }
        } else {
            hilSimulatorRegressionNavigationReadySinceMs = 0L;
        }
        boolean simulatorStartReady = simulatorRawReady
                && hilSimulatorRegressionNavigationReadySinceMs > 0L
                && now - hilSimulatorRegressionNavigationReadySinceMs >= 1_500L;
        if (rawFresh) {
            hilSimulatorRegressionSawMotors |= sample.getMotorsOn();
            hilSimulatorRegressionSawFlying |= sample.getFlying();
            hilSimulatorRegressionMaxRawHz = Math.max(
                    hilSimulatorRegressionMaxRawHz, sample.getMeasuredRateHz());
            if (hilSimulatorRegressionOrigin != null) {
                hilSimulatorRegressionMaxAltitudeGain = Math.max(
                        hilSimulatorRegressionMaxAltitudeGain,
                        -sample.getDownMeters() + hilSimulatorRegressionOrigin.getDownMeters());
            }
        }
        if (now - hilSimulatorRegressionLastWaitLogMs >= 1_000L) {
            hilSimulatorRegressionLastWaitLogMs = now;
            AndroidHilController.Status status = hilController.latestStatus();
            appendLog("HIL_REGRESSION WAIT stage=" + hilSimulatorRegressionStage
                    + " connected=" + aircraftSnapshot.getConnected()
                    + " simulator=" + aircraftSnapshot.getSimulatorActive()
                    + " rawFresh=" + rawFresh
                    + " rawReady=" + simulatorRawReady
                    + " navReady=" + navigationReady
                    + " startReady=" + simulatorStartReady
                    + " startPending=" + hilSimulatorAutoStartPending
                    + " peer=" + (status != null && status.getPeerFresh())
                    + " peerPose=" + (hilSimulatorRegressionLoopbackPeer == null
                    ? 0L : hilSimulatorRegressionLoopbackPeer.poseCount()));
        }
        if (hilSimulatorRegressionStage == 0) {
            if (!aircraftSnapshot.getConnected()
                    && now - hilSimulatorRegressionStartedMs >= 10_000L) {
                finishHilSimulatorRegression(false, "DJI product unavailable after 10s");
                return;
            }
            ensureHilSimulatorStarted();
            if (!simulatorStartReady) {
                mainHandler.postDelayed(hilSimulatorRegressionRunnable, 100L);
                return;
            }
            hilSimulatorRegressionOrigin = sample;
            hilSimulatorRegressionBaseline = sample;
            appendLog(formatHilRegressionSample("BASELINE", sample));
            appendLog("HIL_REGRESSION DIAGNOSTICS "
                    + aircraftBridge.simulatorRegressionDiagnostics());
            hilSimulatorRegressionStage = 3;
            hilSimulatorRegressionStageStartedMs = now;
            appendLog("HIL_REGRESSION TAKEOFF_ARMING native=true preTakeoffVS=false settle=1500ms");
            mainHandler.postDelayed(() -> {
                if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 3) return;
                requestHilSimulatorRegressionTakeoff();
            }, 1_500L);
            return;
        }
        if (!simulatorRawReady) {
            finishHilSimulatorRegression(false, "raw SimulatorState lost during command sequence");
            return;
        }
        if (hilSimulatorRegressionStage == 3) {
            mainHandler.postDelayed(hilSimulatorRegressionRunnable, 100L);
            return;
        }
        if (hilSimulatorRegressionStage == 4) {
            if (hilSimulatorRegressionMotorFallback
                    && !aircraftBridge.sendSimulatorRegressionSticks(0, 330, 0, 0)) {
                finishHilSimulatorRegression(false, "motor fallback upward VS command blocked");
                return;
            }
            boolean takeoffConfirmed = hilSimulatorRegressionSawMotors
                    && hilSimulatorRegressionSawFlying
                    && hilSimulatorRegressionMaxAltitudeGain >= 0.05
                    && (hilSimulatorRegressionMotorFallback
                    || aircraftBridge.isSimulatorTakeoffTransitionComplete());
            if (!takeoffConfirmed) {
                if (now - hilSimulatorRegressionStageStartedMs >= 7_000L) {
                    finishHilSimulatorRegression(false, String.format(Locale.US,
                            "takeoff evidence missing motors=%s flying=%s altitude=%.3fm takeoff={%s}",
                            hilSimulatorRegressionSawMotors, hilSimulatorRegressionSawFlying,
                            hilSimulatorRegressionMaxAltitudeGain, hilSimulatorRegressionTakeoffResult));
                    return;
                }
                mainHandler.postDelayed(hilSimulatorRegressionRunnable, 100L);
                return;
            }
            appendLog(String.format(Locale.US,
                    "HIL_REGRESSION TAKEOFF_CONFIRMED motors=%s flying=%s altitude=%.3fm raw=%.1fHz",
                    hilSimulatorRegressionSawMotors, hilSimulatorRegressionSawFlying,
                    hilSimulatorRegressionMaxAltitudeGain, hilSimulatorRegressionMaxRawHz));
            if (hilSimulatorRegressionMotorFallback) {
                aircraftBridge.sendSimulatorRegressionSticks(0, 0, 0, 0);
            }
            hilSimulatorRegressionBaseline = sample;
            hilSimulatorRegressionMaxForwardMovement = 0.0;
            hilSimulatorRegressionMaxForwardAttitude = 0.0;
            if (aircraftSnapshot.getVirtualStickEnabled()) {
                hilSimulatorRegressionStage = 5;
                hilSimulatorRegressionStageStartedMs = now;
                mainHandler.post(hilSimulatorRegressionRunnable);
                return;
            }
            hilSimulatorRegressionStage = 6;
            hilSimulatorRegressionStageStartedMs = now;
            aircraftBridge.enableSimulatorRegressionVirtualStick((ok, message) -> {
                if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 6) return;
                appendLog("HIL_REGRESSION POST_TAKEOFF_VS_ENABLE ok=" + ok
                        + " message=" + message);
                if (!ok) {
                    finishHilSimulatorRegression(false,
                            "VS re-enable after takeoff failed: " + message);
                    return;
                }
                hilSimulatorRegressionStage = 5;
                hilSimulatorRegressionStageStartedMs = SystemClock.elapsedRealtime();
                mainHandler.post(hilSimulatorRegressionRunnable);
            });
            return;
        }
        if (hilSimulatorRegressionStage == 6) {
            mainHandler.postDelayed(hilSimulatorRegressionRunnable, 100L);
            return;
        }
        if (hilSimulatorRegressionStage == 5) {
            if (!aircraftBridge.sendSimulatorRegressionSticks(0, 0, 0, 165)) {
                finishHilSimulatorRegression(false, "forward VS command blocked");
                return;
            }
            hilSimulatorRegressionMaxForwardMovement = Math.max(
                    hilSimulatorRegressionMaxForwardMovement,
                    hilRegressionPositionDelta(hilSimulatorRegressionBaseline, sample));
            hilSimulatorRegressionMaxForwardAttitude = Math.max(
                    hilSimulatorRegressionMaxForwardAttitude,
                    hilRegressionAttitudeDelta(hilSimulatorRegressionBaseline, sample));
            if (now - hilSimulatorRegressionStageStartedMs < 2_000L) {
                mainHandler.postDelayed(hilSimulatorRegressionRunnable, 40L);
                return;
            }
            aircraftBridge.sendSimulatorRegressionSticks(0, 0, 0, 0);
            appendLog(formatHilRegressionSample("AFTER_FORWARD_PULSE", sample));
            double minimumRawHz = HilRawRegressionCriteria.minimumFrequencyHz(hilSimulatorStateHz);
            boolean rawPassed = HilRawRegressionCriteria.rawPass(
                    hilSimulatorStateHz, hilSimulatorRegressionMaxRawHz,
                    hilSimulatorRegressionSawMotors, hilSimulatorRegressionSawFlying,
                    hilSimulatorRegressionMaxAltitudeGain, hilSimulatorRegressionMaxForwardMovement,
                    hilSimulatorRegressionMaxForwardAttitude);
            finishHilSimulatorRegression(rawPassed,
                    String.format(Locale.US,
                            "motors=%s flying=%s altitude=%.3fm raw=%.1f/%.1fHz "
                                    + "forward=%.3fm attitude=%.2fdeg takeoff={%s}",
                            hilSimulatorRegressionSawMotors, hilSimulatorRegressionSawFlying,
                            hilSimulatorRegressionMaxAltitudeGain, hilSimulatorRegressionMaxRawHz,
                            minimumRawHz, hilSimulatorRegressionMaxForwardMovement,
                            hilSimulatorRegressionMaxForwardAttitude, hilSimulatorRegressionTakeoffResult));
        }
    }

    private void requestHilSimulatorRegressionTakeoff() {
        if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 3) return;
        if (hilSimulatorCommandPoisoned) {
            finishHilSimulatorRegression(false,
                    "Simulator command fuse active: " + hilSimulatorCommandPoisonReason);
            return;
        }
        if (aircraftBridge == null || !aircraftBridge.isSimulatorRegressionReady()) {
            finishHilSimulatorRegression(false, "Simulator not ready before takeoff retry");
            return;
        }
        int attempt = ++hilSimulatorRegressionTakeoffAttempts;
        appendLog("HIL_REGRESSION TAKEOFF_REQUEST attempt=" + attempt + " "
                + aircraftBridge.simulatorRegressionDiagnostics());
        aircraftBridge.requestSimulatorRegressionTakeoff((ok, message) -> {
            if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 3) return;
            hilSimulatorRegressionTakeoffResult = "attempt=" + attempt
                    + " ok=" + ok + " message=" + message;
            appendLog("HIL_REGRESSION TAKEOFF " + hilSimulatorRegressionTakeoffResult);
            if (ok) {
                hilSimulatorCommandPoisoned = false;
                hilSimulatorCommandPoisonReason = "";
                hilSimulatorRegressionStage = 4;
                hilSimulatorRegressionStageStartedMs = SystemClock.elapsedRealtime();
                mainHandler.postDelayed(hilSimulatorRegressionRunnable, 100L);
                return;
            }
            if (isHilSimulatorCommandPoison(message)) {
                appendLog("HIL_REGRESSION native takeoff poisoned; trying simulator-only motor fallback");
                aircraftBridge.requestSimulatorRegressionMotorsOn((motorsOk, motorsMessage) -> {
                    if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 3) return;
                    appendLog("HIL_REGRESSION POISON_MOTORS_ON ok=" + motorsOk
                            + " message=" + motorsMessage);
                    if (!motorsOk) {
                        tripHilSimulatorCommandFuse(message + "; motors=" + motorsMessage);
                        finishHilSimulatorRegression(false,
                                "native takeoff and motor fallback rejected: " + motorsMessage);
                        return;
                    }
                    hilSimulatorRegressionMotorFallback = true;
                    hilSimulatorRegressionTakeoffResult += "; poisonMotorFallback=" + motorsMessage;
                    hilSimulatorRegressionStage = 6;
                    aircraftBridge.enableSimulatorRegressionVirtualStick((vsOk, vsMessage) -> {
                        if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 6) return;
                        appendLog("HIL_REGRESSION POISON_MOTOR_VS ok=" + vsOk
                                + " message=" + vsMessage);
                        if (!vsOk) {
                            finishHilSimulatorRegression(false,
                                    "poison motor fallback VS enable failed: " + vsMessage);
                            return;
                        }
                        hilSimulatorRegressionStage = 4;
                        hilSimulatorRegressionStageStartedMs = SystemClock.elapsedRealtime();
                        mainHandler.post(hilSimulatorRegressionRunnable);
                    });
                });
                return;
            }
            if (attempt < 3) {
                appendLog("HIL_REGRESSION TAKEOFF retrying after 750ms");
                mainHandler.postDelayed(this::requestHilSimulatorRegressionTakeoff, 750L);
                return;
            }
            appendLog("HIL_REGRESSION TAKEOFF rejected after " + attempt
                    + " attempts; trying precision takeoff");
            aircraftBridge.requestSimulatorRegressionPrecisionTakeoff((precisionOk, precisionMessage) -> {
                if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 3) return;
                appendLog("HIL_REGRESSION PRECISION_TAKEOFF ok=" + precisionOk
                        + " message=" + precisionMessage);
                if (precisionOk) {
                    hilSimulatorRegressionTakeoffResult += "; precision=" + precisionMessage;
                    hilSimulatorRegressionStage = 4;
                    hilSimulatorRegressionStageStartedMs = SystemClock.elapsedRealtime();
                    mainHandler.postDelayed(hilSimulatorRegressionRunnable, 100L);
                    return;
                }
                appendLog("HIL_REGRESSION precision takeoff rejected; trying explicit motor start");
                aircraftBridge.requestSimulatorRegressionMotorsOn((motorsOk, motorsMessage) -> {
                    if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 3) return;
                    appendLog("HIL_REGRESSION MOTORS_ON ok=" + motorsOk
                            + " message=" + motorsMessage);
                    if (!motorsOk) {
                        finishHilSimulatorRegression(false,
                                "takeoff/precision/motor start all rejected: " + motorsMessage);
                        return;
                    }
                    hilSimulatorRegressionMotorFallback = true;
                    hilSimulatorRegressionTakeoffResult += "; motorFallback=" + motorsMessage;
                    hilSimulatorRegressionStage = 6;
                    aircraftBridge.enableSimulatorRegressionVirtualStick((vsOk, vsMessage) -> {
                        if (!hilSimulatorRegressionActive || hilSimulatorRegressionStage != 6) return;
                        appendLog("HIL_REGRESSION MOTOR_FALLBACK_VS_ENABLE ok=" + vsOk
                                + " message=" + vsMessage);
                        if (!vsOk) {
                            finishHilSimulatorRegression(false,
                                    "motor fallback VS enable failed: " + vsMessage);
                            return;
                        }
                        hilSimulatorRegressionStage = 4;
                        hilSimulatorRegressionStageStartedMs = SystemClock.elapsedRealtime();
                        mainHandler.post(hilSimulatorRegressionRunnable);
                    });
                });
            });
        });
    }

    private void finishHilSimulatorRegression(boolean passed, String reason) {
        if (!hilSimulatorRegressionActive) return;
        hilSimulatorRegressionActive = false;
        int cleanupGeneration = ++hilSimulatorRegressionGeneration;
        hilSimulatorRegressionCleanupActive = true;
        mainHandler.removeCallbacks(hilSimulatorRegressionRunnable);
        AndroidHilController.Status status = hilController == null ? null : hilController.latestStatus();
        long loopbackPoses = hilSimulatorRegressionLoopbackPeer == null
                ? 0L : hilSimulatorRegressionLoopbackPeer.poseCount();
        double minimumPoseHz = HilRawRegressionCriteria.minimumFrequencyHz(hilSimulatorStateHz);
        boolean linkOk = status != null && HilRawRegressionCriteria.linkPass(
                hilSimulatorStateHz, status.getPeerFresh(), status.getSentPoseCount(),
                loopbackPoses, status.getMeasuredPoseSendHz());
        boolean finalPassed = passed && linkOk;
        String link = String.format(Locale.US,
                "peer=%s sent=%d receivedPose=%d rate=%.1f/%.1fHz",
                status != null && status.getPeerFresh(),
                status == null ? 0L : status.getSentPoseCount(), loopbackPoses,
                status == null ? 0.0 : status.getMeasuredPoseSendHz(), minimumPoseHz);
        appendLog("HIL_REGRESSION CLEANUP start zero=true vs="
                + aircraftSnapshot.getVirtualStickEnabled() + " simulatorOwned=" + hilStartedSimulator);
        mainHandler.postDelayed(() -> {
            if (!hilSimulatorRegressionCleanupActive
                    || cleanupGeneration != hilSimulatorRegressionGeneration) return;
            if (aircraftBridge != null) {
                if (aircraftSnapshot.getVirtualStickEnabled()) {
                    aircraftBridge.disableVirtualStick(getString(R.string.hil_cleanup_timeout_fallback));
                }
                if (hilStartedSimulator && aircraftSnapshot.getSimulatorActive()) {
                    aircraftBridge.setSimulatorEnabled(false);
                }
            }
            completeHilSimulatorRegressionCleanup(
                    cleanupGeneration, false, finalPassed, link, reason,
                    "cleanup timeout after 30s");
        }, 30_000L);
        if (aircraftBridge == null) {
            completeHilSimulatorRegressionCleanup(
                    cleanupGeneration, false, finalPassed, link, reason,
                    "aircraft bridge unavailable during cleanup");
            return;
        }
        aircraftBridge.sendSimulatorRegressionSticks(0, 0, 0, 0);
        if (aircraftSnapshot.getVirtualStickEnabled()) {
            aircraftBridge.disableSimulatorRegressionVirtualStick((ok, message) ->
                    continueHilSimulatorRegressionCleanup(
                            cleanupGeneration, finalPassed, link, reason, ok, message));
        } else {
            continueHilSimulatorRegressionCleanup(
                    cleanupGeneration, finalPassed, link, reason, true, "already disabled");
        }
    }

    private void continueHilSimulatorRegressionCleanup(
            int cleanupGeneration, boolean candidatePassed, String link, String reason,
            boolean virtualStickOk, String virtualStickMessage) {
        if (!hilSimulatorRegressionCleanupActive
                || cleanupGeneration != hilSimulatorRegressionGeneration) return;
        appendLog("HIL_REGRESSION CLEANUP vsOk=" + virtualStickOk
                + " message=" + virtualStickMessage);
        if (!hilStartedSimulator || aircraftBridge == null
                || !aircraftSnapshot.getSimulatorActive()) {
            completeHilSimulatorRegressionCleanup(
                    cleanupGeneration, virtualStickOk, candidatePassed, link, reason,
                    "vsOk=" + virtualStickOk + " message=" + virtualStickMessage
                            + " simulator=" + (hilStartedSimulator ? "already stopped" : "not owned"));
            return;
        }
        Mini2AircraftBridge.SimulatorSample sample = aircraftBridge.currentSimulatorSample();
        boolean airborne = sample != null && (sample.getMotorsOn() || sample.getFlying());
        if (!airborne) {
            stopHilSimulatorAfterRegressionCleanup(
                    cleanupGeneration, candidatePassed, link, reason,
                    virtualStickOk, virtualStickMessage, true, "already landed");
            return;
        }
        aircraftBridge.requestSimulatorRegressionLanding((landingOk, landingMessage) -> {
            appendLog("HIL_REGRESSION CLEANUP landingOk=" + landingOk
                    + " message=" + landingMessage);
            if (!landingOk) {
                stopHilSimulatorAfterRegressionCleanup(
                        cleanupGeneration, candidatePassed, link, reason,
                        virtualStickOk, virtualStickMessage, false,
                        "landing command failed: " + landingMessage);
                return;
            }
            waitForHilSimulatorLanding(
                    cleanupGeneration, candidatePassed, link, reason,
                    virtualStickOk, virtualStickMessage,
                    SystemClock.elapsedRealtime(), landingMessage);
        });
    }

    private void waitForHilSimulatorLanding(
            int cleanupGeneration, boolean candidatePassed, String link, String reason,
            boolean virtualStickOk, String virtualStickMessage,
            long landingStartedMs, String landingMessage) {
        if (!hilSimulatorRegressionCleanupActive
                || cleanupGeneration != hilSimulatorRegressionGeneration) return;
        Mini2AircraftBridge.SimulatorSample sample = aircraftBridge == null
                ? null : aircraftBridge.currentSimulatorSample();
        boolean landed = sample != null && !sample.getMotorsOn() && !sample.getFlying();
        long now = SystemClock.elapsedRealtime();
        if (sample != null && now - hilSimulatorRegressionLastLandingLogMs >= 1_000L) {
            hilSimulatorRegressionLastLandingLogMs = now;
            appendLog(formatHilRegressionSample("LANDING", sample)
                    + " confirmNeeded=" + aircraftSnapshot.getLandingConfirmationNeeded());
        }
        if (aircraftSnapshot.getLandingConfirmationNeeded()
                && !hilSimulatorRegressionLandingConfirmationRequested
                && aircraftBridge != null) {
            hilSimulatorRegressionLandingConfirmationRequested = true;
            aircraftBridge.requestSimulatorRegressionLandingConfirmation((ok, message) ->
                    appendLog("HIL_REGRESSION CLEANUP landingConfirmOk=" + ok
                            + " message=" + message));
        }
        if (landed) {
            appendLog("HIL_REGRESSION CLEANUP landing confirmed motors=false flying=false");
            stopHilSimulatorAfterRegressionCleanup(
                    cleanupGeneration, candidatePassed, link, reason,
                    virtualStickOk, virtualStickMessage, true,
                    "landingOk=true message=" + landingMessage + " landed=true");
            return;
        }
        if (now - landingStartedMs >= 20_000L) {
            appendLog("HIL_REGRESSION CLEANUP landing confirmation timeout");
            stopHilSimulatorAfterRegressionCleanup(
                    cleanupGeneration, candidatePassed, link, reason,
                    virtualStickOk, virtualStickMessage, false,
                    "landingOk=true message=" + landingMessage + " landed=false timeout");
            return;
        }
        mainHandler.postDelayed(() -> waitForHilSimulatorLanding(
                cleanupGeneration, candidatePassed, link, reason,
                virtualStickOk, virtualStickMessage, landingStartedMs, landingMessage), 100L);
    }

    private void stopHilSimulatorAfterRegressionCleanup(
            int cleanupGeneration, boolean candidatePassed, String link, String reason,
            boolean virtualStickOk, String virtualStickMessage,
            boolean landingOk, String landingMessage) {
        if (!hilSimulatorRegressionCleanupActive
                || cleanupGeneration != hilSimulatorRegressionGeneration) return;
        if (candidatePassed && virtualStickOk && landingOk
                && hilSimulatorRegressionCompletedCycles + 1
                < HIL_SIMULATOR_REGRESSION_TARGET_CYCLES) {
            hilSimulatorRegressionCompletedCycles += 1;
            appendLog("HIL_REGRESSION CYCLE_PASS cycle="
                    + hilSimulatorRegressionCompletedCycles + "/"
                    + HIL_SIMULATOR_REGRESSION_TARGET_CYCLES + " " + reason
                    + " cleanup={vsOk=true " + landingMessage + "}");
            startNextHilSimulatorRegressionCycle(cleanupGeneration);
            return;
        }
        if (aircraftBridge == null || !aircraftSnapshot.getSimulatorActive()) {
            completeHilSimulatorRegressionCleanup(
                    cleanupGeneration, virtualStickOk && landingOk,
                    candidatePassed, link, reason,
                    "vsOk=" + virtualStickOk + " vsMessage=" + virtualStickMessage
                            + " " + landingMessage + " simulator=already stopped");
            return;
        }
        aircraftBridge.setSimulatorEnabled(false, (ok, message) -> {
            appendLog("HIL_REGRESSION CLEANUP simulatorStopOk=" + ok + " message=" + message);
            completeHilSimulatorRegressionCleanup(
                    cleanupGeneration, virtualStickOk && landingOk && ok,
                    candidatePassed, link, reason,
                    "vsOk=" + virtualStickOk + " vsMessage=" + virtualStickMessage
                            + " " + landingMessage + " simulatorStopOk=" + ok
                            + " message=" + message);
        });
    }

    private void startNextHilSimulatorRegressionCycle(int cleanupGeneration) {
        if (!hilSimulatorRegressionCleanupActive
                || cleanupGeneration != hilSimulatorRegressionGeneration) return;
        hilSimulatorRegressionCleanupActive = false;
        hilSimulatorRegressionActive = true;
        hilSimulatorRegressionStage = 0;
        hilSimulatorRegressionStartedMs = SystemClock.elapsedRealtime();
        hilSimulatorRegressionStageStartedMs = hilSimulatorRegressionStartedMs;
        hilSimulatorRegressionLastWaitLogMs = 0L;
        int generation = ++hilSimulatorRegressionGeneration;
        hilSimulatorRegressionOrigin = null;
        hilSimulatorRegressionBaseline = null;
        hilSimulatorRegressionTakeoffResult = "not-run";
        hilSimulatorRegressionSawMotors = false;
        hilSimulatorRegressionSawFlying = false;
        hilSimulatorRegressionMaxRawHz = 0.0;
        hilSimulatorRegressionMaxAltitudeGain = 0.0;
        hilSimulatorRegressionMaxForwardMovement = 0.0;
        hilSimulatorRegressionMaxForwardAttitude = 0.0;
        hilSimulatorRegressionTakeoffAttempts = 0;
        hilSimulatorRegressionMotorFallback = false;
        hilSimulatorRegressionNavigationReadySinceMs = 0L;
        hilSimulatorRegressionLandingConfirmationRequested = false;
        hilSimulatorRegressionLastLandingLogMs = 0L;
        hilSimulatorAutoStartPending = false;
        hilSimulatorStartInFlight = false;
        appendLog("HIL_REGRESSION CYCLE_START cycle="
                + (hilSimulatorRegressionCompletedCycles + 1) + "/"
                + HIL_SIMULATOR_REGRESSION_TARGET_CYCLES
                + " source=DJI SimulatorState RAW synthetic=false simulator=reused");
        mainHandler.post(hilSimulatorRegressionRunnable);
        modelExecutor.execute(() -> {
            try {
                Thread.sleep(32_000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
            mainHandler.post(() -> {
                if (hilSimulatorRegressionActive
                        && generation == hilSimulatorRegressionGeneration) {
                    finishHilSimulatorRegression(false,
                            "independent 32s watchdog timeout cycle="
                                    + (hilSimulatorRegressionCompletedCycles + 1));
                }
            });
        });
    }

    private void completeHilSimulatorRegressionCleanup(
            int cleanupGeneration, boolean cleanupOk, boolean candidatePassed,
            String link, String reason, String cleanupReason) {
        if (!hilSimulatorRegressionCleanupActive
                || cleanupGeneration != hilSimulatorRegressionGeneration) return;
        hilSimulatorRegressionCleanupActive = false;
        hilSimulatorRegressionOwnsLink = false;
        hilSimulatorStartGeneration += 1;
        hilSimulatorStartAttempts = 0;
        hilSimulatorAutoStartPending = false;
        hilSimulatorStartInFlight = false;
        hilStartedSimulator = false;
        if (hilController != null) hilController.stop();
        if (hilSimulatorRegressionLoopbackPeer != null) {
            hilSimulatorRegressionLoopbackPeer.close();
            hilSimulatorRegressionLoopbackPeer = null;
        }
        if (candidatePassed && cleanupOk) hilSimulatorRegressionCompletedCycles += 1;
        boolean finalPassed = candidatePassed && cleanupOk
                && hilSimulatorRegressionCompletedCycles
                >= HIL_SIMULATOR_REGRESSION_TARGET_CYCLES;
        String finalReason = "cycles=" + hilSimulatorRegressionCompletedCycles + "/"
                + HIL_SIMULATOR_REGRESSION_TARGET_CYCLES + " " + reason
                + " cleanup={" + cleanupReason + "}";
        appendLog("HIL_REGRESSION RESULT=" + (finalPassed ? "PASS" : "FAIL")
                + " source=DJI SimulatorState RAW synthetic=false " + link
                + " reason=" + finalReason);
        Log.i(TAG, "HIL_REGRESSION RESULT=" + (finalPassed ? "PASS" : "FAIL")
                + " " + link + " " + finalReason);
    }

    private String formatHilRegressionSample(
            String label, Mini2AircraftBridge.SimulatorSample sample) {
        return String.format(Locale.US,
                "HIL_REGRESSION %s seq=%d xyz=%.3f,%.3f,%.3f rpy=%.2f,%.2f,%.2f motors=%s flying=%s rate=%.1fHz",
                label, sample.getSequence(), sample.getEastMeters(), sample.getNorthMeters(),
                -sample.getDownMeters(), sample.getRollDegrees(), sample.getPitchDegrees(),
                sample.getYawDegrees(), sample.getMotorsOn(), sample.getFlying(),
                sample.getMeasuredRateHz());
    }

    private double hilRegressionPositionDelta(
            Mini2AircraftBridge.SimulatorSample start,
            Mini2AircraftBridge.SimulatorSample end) {
        if (start == null || end == null) return 0.0;
        double east = end.getEastMeters() - start.getEastMeters();
        double north = end.getNorthMeters() - start.getNorthMeters();
        double down = end.getDownMeters() - start.getDownMeters();
        return Math.sqrt(east * east + north * north + down * down);
    }

    private double hilRegressionAttitudeDelta(
            Mini2AircraftBridge.SimulatorSample start,
            Mini2AircraftBridge.SimulatorSample end) {
        if (start == null || end == null) return 0.0;
        return Math.max(Math.abs(end.getRollDegrees() - start.getRollDegrees()),
                Math.max(Math.abs(end.getPitchDegrees() - start.getPitchDegrees()),
                        Math.abs(end.getYawDegrees() - start.getYawDegrees())));
    }

    /** Debug-only deterministic entry point for emulator regression; calls the same handlers as UI. */
    private void handleSurveyUiDryRunControl(String command) {
        appendLog("SURVEY UI TEST command=" + command);
        if ("preflight".equals(command)) {
            runSurveySimulatorGateCheck();
        } else if ("start".equals(command)) {
            startSurveySimulatorExecution();
        } else if ("pause".equals(command)) {
            if (surveySimulatorExecution != null
                    && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.PAUSED
                    && surveyUiDryRunMode) {
                appendLog("SURVEY UI DRY RUN PAUSED · NO_CONTROL");
                renderSurveySimulatorExecutionStatus(surveySimulatorExecution.getStatus(), null);
            } else {
                pauseSurveySimulatorExecution();
            }
        } else if ("resume".equals(command)) {
            if (surveySimulatorExecution != null
                    && surveySimulatorExecution.getStatus().getState() == SurveyExecutionState.PAUSED) {
                startSurveySimulatorExecution();
            }
        } else if ("abort".equals(command)) {
            abortSurveySimulatorExecution("Debug UI regression requested abort", true);
        } else if ("select_forward_right".equals(command)) {
            if (surveyEnabledCaptureViews.contains(SurveyCaptureView.NADIR)) {
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.NADIR);
            }
            if (surveyEnabledCaptureViews.contains(SurveyCaptureView.BACKWARD_OBLIQUE)) {
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.BACKWARD_OBLIQUE);
            }
            if (surveyEnabledCaptureViews.contains(SurveyCaptureView.LEFT_OBLIQUE)) {
                toggleSurveyCaptureViewEnabled(SurveyCaptureView.LEFT_OBLIQUE);
            }
            appendLog("SURVEY UI TEST selected forward+right groups="
                    + surveyCaptureViewSelectionLabel());
        } else if ("show_capture".equals(command)) {
            showSurveyPlannerTab(SURVEY_TAB_CAPTURE);
            appendLog("SURVEY UI TEST capture tab visible");
        } else {
            appendLog("SURVEY UI TEST ignored unknown command=" + command);
        }
    }

    private void generateDebugSurveyRegression(Intent intent) {
        String requestedMode = intent.getStringExtra("mode");
        boolean fiveDirection = "five_direction".equals(requestedMode);
        boolean terrainDsm = "terrain_dsm".equals(requestedMode);
        Object requestedLatitude = intent.hasExtra("latitude")
                ? intent.getExtras().get("latitude") : null;
        Object requestedLongitude = intent.hasExtra("longitude")
                ? intent.getExtras().get("longitude") : null;
        double latitude = requestedLatitude instanceof Number
                ? ((Number) requestedLatitude).doubleValue()
                : Double.isFinite(aircraftSnapshot.getLatitude())
                        ? aircraftSnapshot.getLatitude() : 31.2304;
        double longitude = requestedLongitude instanceof Number
                ? ((Number) requestedLongitude).doubleValue()
                : Double.isFinite(aircraftSnapshot.getLongitude())
                        ? aircraftSnapshot.getLongitude() : 121.4737;
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || Math.abs(latitude) > 85.0 || Math.abs(longitude) > 180.0) {
            appendLog("SURVEY debug regression rejected invalid center");
            return;
        }
        GeoPoint center = new GeoPoint(latitude, longitude, 0.0);
        surveyDebugPreviewTakeoffPoint = center;
        SurveyMission regression = SurveyRegressionMissionFactory.create(center, fiveDirection, this);
        if (terrainDsm) {
            double minLat = regression.getRoi().stream().mapToDouble(GeoPoint::getLatitude).min().getAsDouble();
            double maxLat = regression.getRoi().stream().mapToDouble(GeoPoint::getLatitude).max().getAsDouble();
            double minLon = regression.getRoi().stream().mapToDouble(GeoPoint::getLongitude).min().getAsDouble();
            double maxLon = regression.getRoi().stream().mapToDouble(GeoPoint::getLongitude).max().getAsDouble();
            TerrainRasterInfo info = new TerrainRasterInfo(
                    "debug-buildings-dsm.tif", 256, 192, 4326, null,
                    (maxLon - minLon) / 256.0, (maxLat - minLat) / 192.0,
                    minLat, maxLat, minLon, maxLon);
            TerrainElevationSource terrain = new TerrainElevationSource() {
                @Override public TerrainRasterInfo getInfo() { return info; }
                @Override public double elevationMeters(double lat, double lon) {
                    boolean buildingA = lon > minLon + (maxLon - minLon) * 0.25
                            && lon < minLon + (maxLon - minLon) * 0.48
                            && lat > minLat + (maxLat - minLat) * 0.22
                            && lat < minLat + (maxLat - minLat) * 0.58;
                    boolean buildingB = lon > minLon + (maxLon - minLon) * 0.62
                            && lon < minLon + (maxLon - minLon) * 0.82
                            && lat > minLat + (maxLat - minLat) * 0.55
                            && lat < minLat + (maxLat - minLat) * 0.80;
                    return buildingA ? 128.0 : buildingB ? 118.0 : 100.0;
                }
            };
            SurveyTerrainPlanResult result = SurveyTerrainPlanner.INSTANCE.apply(
                    regression, terrain, center,
                    "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
                    SurveyTerrainPlanner.DEFAULT_SAMPLE_SPACING_METERS, null,
                    new SurveyTerrainTakeoffReference(
                            center, SurveyTerrainTakeoffReferenceSource.AIRCRAFT_LOCATION,
                            System.currentTimeMillis()));
            int columns = 96;
            int rows = 64;
            double[] grid = new double[columns * rows];
            for (int row = 0; row < rows; row++) for (int column = 0; column < columns; column++) {
                double lat = maxLat - (maxLat - minLat) * row / (rows - 1.0);
                double lon = minLon + (maxLon - minLon) * column / (columns - 1.0);
                grid[row * columns + column] = terrain.elevationMeters(lat, lon);
            }
            setSurveyTerrainFollowingEnabled(true, false);
            showSurveyPlanner(true);
            showSurveyPlannerTab(SURVEY_TAB_TERRAIN);
            completeGeneratedSurveyMission(result.getMission(), result.getSafety(),
                    new TerrainPreviewData(info, grid, columns, rows));
            TextView dsmStatus = findViewById(R.id.survey_dsm_status);
            dsmStatus.setText(R.string.debug_dsm_loaded);
            dsmStatus.setTextColor(0xFF2D6B3F);
            renderSurveyStatus(getString(R.string.debug_dsm_verify));
            appendLog("SURVEY debug building DSM generated · NO CONTROL");
            return;
        }
        activateSurveyMission(regression, getString(R.string.debug_standard_regression_loaded));
        renderSurveyStatus(getString(R.string.debug_regression_loaded,
                surveySummary(surveyMission)));
        appendLog("SURVEY debug regression generated mode="
                + (fiveDirection ? "five_direction" : "ortho")
                + " waypoints=" + surveyMission.getWaypoints().size()
                + " photos=" + surveyMission.getEstimatedPhotoCount() + " · NO_CONTROL");
    }

    private void runDebugModelSmoke(String requestedPrompt) {
        final String prompt = requestedPrompt == null || requestedPrompt.trim().isEmpty()
                ? "Fly forward toward the target and stop at a safe distance."
                : requestedPrompt.trim();
        modelExecutor.execute(() -> {
            Bitmap frame = Bitmap.createBitmap(MODEL_FRAME_WIDTH, MODEL_FRAME_HEIGHT, Bitmap.Config.ARGB_8888);
            frame.eraseColor(Color.rgb(74, 101, 118));
            try {
                InferenceControlClient.Result result = localRuntime().infer(
                        frame, prompt, Mini2MockSnapshots.create("armed", this), false, executedPrefix);
                Log.i(TAG, "MODEL_SMOKE status=" + result.getStatusCode() + " ok=" + result.getOk()
                        + " response=" + result.getMessage().substring(
                                0, Math.min(2_000, result.getMessage().length())));
                showModelResult("smoke", result);
            } catch (Throwable error) {
                Log.e(TAG, "MODEL_SMOKE failed", error);
                showModelFailure(getString(R.string.model_failure_smoke), error, false);
            } finally {
                frame.recycle();
            }
        });
    }

    private void applyMockUiState(String requestedMode) {
        String mode = requestedMode == null ? "ground" : requestedMode;
        mockUiActive = true;
        Mini2AircraftBridge.Snapshot snapshot = Mini2MockSnapshots.create(mode, this);
        aircraftSnapshot = snapshot;
        long now = SystemClock.elapsedRealtime();
        lastTelemetryAtElapsedMs = now;
        boolean cameraUnavailable = "manual_no_camera".equals(mode);
        mockCameraUnavailable = cameraUnavailable;
        lastFrameAtElapsedMs = cameraUnavailable ? 0L : now;
        packetCount = Math.max(packetCount, 1L);
        modelLoaded = !"disconnected".equals(mode) && !cameraUnavailable;
        emergencyStopped = "emergency".equals(mode);
        controlArmed = "armed".equals(mode) || "thinking".equals(mode);
        inferenceInFlight = "thinking".equals(mode);
        latestInferenceLatencyMs = "armed".equals(mode) ? 684L : null;

        View background = findViewById(R.id.mock_video_background);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] {0xFF243846, 0xFF1B2630, 0xFF5A3F32});
        background.setBackground(gradient);
        background.setVisibility(View.VISIBLE);
        TextView label = findViewById(R.id.mock_video_label);
        label.setText(getString(
                R.string.simulated_camera_status, mode.toUpperCase(Locale.US)));
        label.setVisibility(View.VISIBLE);
        label.bringToFront();

        Button load = findViewById(R.id.model_load_main);
        load.setText(modelLoaded ? getString(R.string.model_loaded) : getString(R.string.action_load));
        load.setEnabled(!modelLoaded);
        ((Button) findViewById(R.id.auto_infer_button)).setText(getString(R.string.vln_auto_off));
        if (inferenceInFlight) {
            commandText.setText(R.string.vln_thinking);
        } else if ("armed".equals(mode)) {
            commandText.setText("684ms    X 1.0  Y 0.0  Z 0.0  YAW 0.0");
        } else {
            commandText.setText(R.string.vln_idle_command);
        }
        renderAircraftSnapshot(snapshot);
        appendLog("MOCK UI state=" + mode);
    }

    private void requestPermissionsAndRegister() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<String> requiredPermissions = new ArrayList<>();
            for (String permission : PERMISSIONS) requiredPermissions.add(permission);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            boolean missing = false;
            for (String permission : requiredPermissions) {
                missing |= checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
            }
            if (missing) {
                requestPermissions(requiredPermissions.toArray(new String[0]), PERMISSION_REQUEST);
                return;
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) initPublicCaptureStorage();
        registerSdk();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                initPublicCaptureStorage();
            }
            mainHandler.removeCallbacks(phoneMapLocationTimeoutRunnable);
            phoneMapLocationPending = false;
            phoneMapLocationRequested = false;
            if (hasPhoneLocationPermission()) {
                phoneMapLocationResolved = false;
                frameMapNearPhone();
            } else {
                resolvePhoneMapLocationWithoutFix("permission denied");
            }
            registerSdk();
        }
    }

    private void registerSdk() {
        sdkState = "registering";
        DJISDKManager.getInstance().registerApp(getApplicationContext(), sdkCallback);
    }

    private void attachVideoFeed() {
        VideoFeeder feeder = VideoFeeder.getInstance();
        if (feeder == null) return;
        boolean needsTranscodedFeed = feeder.isFetchKeyFrameNeeded()
                || feeder.isLensDistortionCalibrationNeeded();
        String targetName = needsTranscodedFeed ? "transcoded" : "primary";
        if (videoFeed != null && targetName.equals(videoFeedName)) return;
        VideoFeeder.VideoFeed target = needsTranscodedFeed
                ? feeder.provideTranscodedVideoFeed()
                : feeder.getPrimaryVideoFeed();
        if (target == null && needsTranscodedFeed) {
            target = feeder.getPrimaryVideoFeed();
            targetName = "primary-fallback";
        }
        if (target == null || target == videoFeed) return;
        detachVideoFeed();
        videoFeed = target;
        videoFeedName = targetName;
        videoFeed.addVideoDataListener(videoListener);
        Log.i(TAG, videoFeedName + " video listener attached");
    }

    private void detachVideoFeed() {
        if (videoFeed != null) {
            videoFeed.removeVideoDataListener(videoListener);
            videoFeed = null;
            videoFeedName = "none";
        }
    }

    private void resetVideoFeed() {
        detachVideoFeed();
        attachVideoFeed();
    }

    private void rebuildCodec() {
        destroyVideoCodec();
        if (videoSurfaceTexture == null || surfaceWidth <= 0 || surfaceHeight <= 0) return;
        codecManager = new DJICodecManager(
                getApplicationContext(), videoSurfaceTexture, surfaceWidth, surfaceHeight);
        Log.i(TAG, "codec texture ready " + surfaceWidth + "x" + surfaceHeight);
    }

    private void destroyVideoCodec() {
        if (codecManager == null) return;
        codecManager.cleanSurface();
        codecManager.destroyCodec();
        codecManager = null;
    }

    @Override public void onSurfaceTextureAvailable(
            SurfaceTexture surface, int width, int height) {
        videoSurfaceTexture = surface;
        surfaceWidth = width;
        surfaceHeight = height;
        rebuildCodec();
    }

    @Override public void onSurfaceTextureSizeChanged(
            SurfaceTexture surface, int width, int height) {
        videoSurfaceTexture = surface;
        surfaceWidth = width;
        surfaceHeight = height;
        if (codecManager != null) {
            codecManager.onSurfaceSizeChanged(width, height, 0);
            Log.i(TAG, "codec texture resized " + width + "x" + height);
        } else {
            rebuildCodec();
        }
    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        destroyVideoCodec();
        videoSurfaceTexture = null;
        surfaceWidth = 0;
        surfaceHeight = 0;
        return true;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        lastDecodedFrameRenderedElapsedNanos.set(SystemClock.elapsedRealtimeNanos());
        decodedFrameSequence.incrementAndGet();
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override protected void onResume() {
        super.onResume();
        enterImmersiveMode();
        if (!mainUiInitialized) return;
        hilAppInBackground = false;
        if (mapView != null) mapView.onResume();
        if (aircraftBridge != null) aircraftBridge.bindCurrentProduct();
        if (djiAccountController != null) djiAccountController.refresh();
        if (phoneHeadingSource != null) phoneHeadingSource.start();
        // Resume only an unfinished initial activation. Established control and
        // missions remain paused until the user explicitly continues them.
        ensureHilSimulatorStarted();
    }

    @Override protected void onPause() {
        if (!mainUiInitialized) {
            super.onPause();
            return;
        }
        mainHandler.removeCallbacks(persistSurveySettingsRunnable);
        persistSurveyPlannerSettings();
        hilAppInBackground = true;
        String backgroundReason = getString(R.string.app_entered_background);
        invalidateHilSimulatorActivation(backgroundReason);
        pauseHilForTransientSourceLoss(backgroundReason);
        pauseSurveyForLifecycle(backgroundReason);
        if (controlArmed || autoInferenceEnabled || inferenceInFlight) normalStop(backgroundReason);
        mainHandler.removeCallbacks(surveyReplayRunnable);
        if (surveyReplay != null && surveyReplay.getState() == SurveyReplayState.RUNNING) {
            renderSurveyReplay(surveyReplay.pause());
            appendLog("SURVEY preview paused: app background · NO_CONTROL");
        }
        if (mapView != null) mapView.onPause();
        if (phoneHeadingSource != null) phoneHeadingSource.stop();
        super.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mainUiInitialized) return;
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        // Baidu MapView has no low-memory lifecycle callback.
    }

    @Override protected void onDestroy() {
        if (!mainUiInitialized) {
            super.onDestroy();
            return;
        }
        pauseSurveyForLifecycle(getString(R.string.activity_destroyed));
        cancelCameraCadenceTest("activity_destroyed");
        if (surveyLowBatteryReturnDeadlineElapsedMs != 0L && !surveyLowBatteryReturnIssued) {
            surveyLowBatteryReturnIssued = true;
            if (aircraftBridge != null) {
                aircraftBridge.sendBodyVelocity(0f, 0f, 0f, 0f);
                aircraftBridge.disableVirtualStick(getString(R.string.low_battery_rth_before_destroy));
                aircraftBridge.startGoHome();
            }
        } else {
            cancelSurveyLowBatteryReturn(getString(R.string.activity_destroyed));
        }
        mainHandler.removeCallbacksAndMessages(null);
        activityDestroyed = true;
        if (aircraftBridge != null) {
            aircraftBridge.close();
            aircraftBridge = null;
        }
        if (djiAccountController != null) {
            djiAccountController.close();
            djiAccountController = null;
        }
        if (v86Controller != null) {
            v86Controller.close();
            v86Controller = null;
        }
        if (phoneHeadingSource != null) {
            phoneHeadingSource.close();
            phoneHeadingSource = null;
        }
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        recycleMapDescriptor(aircraftMapIcon);
        recycleMapDescriptor(remoteControllerArrowMapIcon);
        recycleMapDescriptor(remoteControllerStaticMapIcon);
        recycleMapDescriptor(homeMapIcon);
        aircraftMapIcon = null;
        remoteControllerArrowMapIcon = null;
        remoteControllerStaticMapIcon = null;
        homeMapIcon = null;
        modelExecutor.shutdownNow();
        for (PendingTriggerFrame pending : pendingTriggerFrames.values()) {
            if (pending.bitmap != null) pending.bitmap.recycle();
        }
        pendingTriggerFrames.clear();
        storageExecutor.shutdown();
        ueBridgeClient.close();
        if (hilController != null) {
            hilController.close();
            hilController = null;
        }
        if (hilOfflineRegressionRunner != null) {
            hilOfflineRegressionRunner.close();
            hilOfflineRegressionRunner = null;
        }
        if (hilSimulatorRegressionLoopbackPeer != null) {
            hilSimulatorRegressionLoopbackPeer.close();
            hilSimulatorRegressionLoopbackPeer = null;
        }
        if (localOpenFlyRuntime != null) {
            localOpenFlyRuntime.close();
            localOpenFlyRuntime = null;
        }
        if (aoaUsbProbe != null) {
            aoaUsbProbe.close();
            aoaUsbProbe = null;
        }
        detachVideoFeed();
        destroyVideoCodec();
        videoSurfaceTexture = null;
        if (isFinishing()) {
            ((Mini2Application) getApplication()).resetHudSessionState();
        }
        super.onDestroy();
    }

    private void recycleMapDescriptor(BitmapDescriptor descriptor) {
        if (descriptor != null) descriptor.recycle();
    }
}
