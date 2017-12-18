package data.management;

/**
 * Copyright 2016 University of Zurich
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import cc.kave.commons.model.events.ActivityEvent;
import cc.kave.commons.model.events.CommandEvent;
import cc.kave.commons.model.events.ErrorEvent;
import cc.kave.commons.model.events.IIDEEvent;
import cc.kave.commons.model.events.InfoEvent;
import cc.kave.commons.model.events.NavigationEvent;
import cc.kave.commons.model.events.SystemEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.userprofiles.UserProfileEvent;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlAction;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlEvent;
import cc.kave.commons.model.events.visualstudio.BuildEvent;
import cc.kave.commons.model.events.visualstudio.BuildTarget;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.DocumentEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import cc.kave.commons.model.events.visualstudio.FindEvent;
import cc.kave.commons.model.events.visualstudio.IDEStateEvent;
import cc.kave.commons.model.events.visualstudio.InstallEvent;
import cc.kave.commons.model.events.visualstudio.SolutionEvent;
import cc.kave.commons.model.events.visualstudio.UpdateEvent;
import cc.kave.commons.model.events.visualstudio.WindowEvent;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import cc.kave.commons.utils.io.json.JsonUtils;
import java.text.ParseException;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple example that shows how the interaction dataset can be opened, all
 * users identified, and all contained events deserialized.
 */
public class Parser {

    private String eventsDir;
    static int commandEvents;
    static int activityEvents;
    static int testse;
    static int versione;
    static int windowe;
    static int builde;
    static int completionevents;
    static int systemevents;
    static int navigationevents;
    static int other;
    static int installevent;
    static int documente;
    static int debuggere;
    static int userprofile;
    static int ideee;
    static int editevent;
    static int finde;
    static int updatevent;
    static int solutionevent;
    static int errore;
    static int infoe;
    static int exceptions;
    static Map<String, ArrayList<String>> ids = new HashMap<>();
    static Map<String, ArrayList<String>> IDESessionUUIDs = new HashMap<>();

    public Parser(String eventsDir) {
        try {
            pw_tmp = new PrintWriter(new FileOutputStream(new File("theme/tmp.txt"), true /* append = true */));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.eventsDir = eventsDir;
    }

    public void run() {

        System.out.printf("looking (recursively) for events in folder %s\n", new File(eventsDir).getAbsolutePath());

        /*
         * Each .zip that is contained in the eventsDir represents all events that we
         * have collected for a specific user, the folder represents the first day when
         * the user uploaded data.
         */
        Set<String> userZips = IoHelper.findAllZips(eventsDir);
        System.out.println(userZips.size());

//      Start PrintWriter for "events"  
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(new File("events.csv"), true /* append = true */));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("file");
        sb.append(",");
        sb.append("IDESessionUUID");
        sb.append(",");
        sb.append("Event");
        sb.append(",");
        sb.append("TriggeredAt");
        sb.append(",");
        sb.append("Duration");
        sb.append("\n");

        pw.write(sb.toString());
        pw.close();

//      Start PrintWriter for "errors" 
        PrintWriter error = null;
        try {
            error = new PrintWriter(new FileOutputStream(new File("errors.csv"), true /* append = true */));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("file");
        sb2.append(",");
        sb2.append("IDESessionUUID");
        sb2.append(",");
        sb2.append("Event");
        sb2.append(",");
        sb2.append("TriggeredAt");
        sb2.append(",");
        sb2.append("Duration");
        sb2.append("\n");

        error.write(sb2.toString());
        error.close();
        for (String userZip : userZips) {
            System.out.printf("\n#### processing user zip: %s #####\n", userZip);
            processUserZip(userZip);     
        }

//      Start PrintWriter for "completion" 
        PrintWriter pw_completion = null;
        try {
            pw_completion = new PrintWriter(new FileOutputStream(new File("completion.csv"), true /* append = true */));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        StringBuilder sb_completion = new StringBuilder();
        sb_completion.append("file");
        sb_completion.append(",");
        sb_completion.append("IDESessionUUID");
        sb_completion.append(",");
        sb_completion.append("Event");
        sb_completion.append(",");
        sb_completion.append("TriggeredAt");
        sb_completion.append(",");
        sb_completion.append("Duration");
        sb_completion.append("\n");

        pw_completion.write(sb_completion.toString());
        pw_completion.close();

        System.out.println("Total files: " + userZips.size());
        System.out.println("Command events: " + commandEvents);
        System.out.println("activity events: " + activityEvents);
        System.out.println("test events: " + testse);
        System.out.println("version cotnrol events: " + versione);
        System.out.println("window events: " + windowe);
        System.out.println("build events: " + builde);
        System.out.println("completion events: " + completionevents);
        System.out.println("system events: " + systemevents);
        System.out.println("navigation events: " + navigationevents);
        System.out.println("other events: " + other);
        System.out.println("install events: " + installevent);
        System.out.println("document events: " + documente);
        System.out.println("debugger events: " + debuggere);
        System.out.println("userprofile events: " + userprofile);
        System.out.println("ideee events: " + ideee);
        System.out.println("edit events: " + editevent);
        System.out.println("find events: " + finde);
        System.out.println("update events: " + updatevent);
        System.out.println("solution events: " + solutionevent);
        System.out.println("error events: " + errore);
        System.out.println("info events: " + infoe);
        System.out.println("EXCEPTIONS : " + exceptions);
        System.out.println("ids: " + ids.size());

        Set<String> keys = ids.keySet();
        StringBuilder sb3 = new StringBuilder();
        sb2.append("id, file\n");
        Map<String, ArrayList<String>> docs_id = new HashMap<String, ArrayList<String>>();

//      Start PrintWriter for "ids_and_zips" 
        PrintWriter pw2 = null;
        try {
            pw2 = new PrintWriter(new FileOutputStream(new File("ids_and_zips.csv"), true /* append = true */));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArrayList<String> docs = new ArrayList<String>();
        for (String s : keys) {
            ArrayList<String> files = ids.get(s);
            System.out.println("ID " + s + " appears in " + files.size());
            for (int i = 0; i < files.size(); i++) {
                if (!docs.contains(files.get(i))) {
                    docs.add(files.get(i));
                    ArrayList<String> pair = new ArrayList<String>();
                    docs_id.put(files.get(i), pair);
                }
                docs_id.get(files.get(i)).add(s);
                sb3.append(s + "," + files.get(i) + "\n");
            }
        }

        pw2.write(sb3.toString());
        pw2.close();

        System.out.println("There are " + docs.size() + " files");
        for (int i = 0; i < docs.size(); i++) {
            boolean cond = true;
            System.out.println("File " + docs.get(i) + " has" + " " + docs_id.get(docs.get(i)).size());
            for (int j = 0; j < docs_id.get(docs.get(i)).size(); j++) {
                //System.out.println("file "+ docs.get(i)+ " id: "+docs_id.get(docs.get(i)).get(j));
                if (j > 1) {
                    if (docs_id.get(docs.get(i)).get(j) != docs_id.get(docs.get(i)).get(j - 1)) {
                        cond = false;
                    }
                }
            }
            System.out.println("This file " + docs.get(i) + " is correct? " + cond);
        }

        System.out.println("Builds succesful: " + build_succesful);
        System.out.println("Builds unsuccesful: " + build_unsuccesful);

        System.out.println("Tests succesful: " + test_succesful);
        System.out.println("Tests unsuccesful: " + test_failed);

        pw_tmp.close();
        System.out.println("----------------------------------------------------");

        // Print the data files 
        PrintWriter pw_theme_all = null;
        StringBuilder sb_tmp_all = new StringBuilder();

        int debug_count = 1;
        int nodebug_count = 1;
        for (String key : IDESessionUUIDs.keySet()) {

            ArrayList<String> arr = IDESessionUUIDs.get(key);
            StringBuilder sb_tmp = new StringBuilder();
            //init data info
            sb_tmp.append("Time" + "\t" + "Event" + "\n");
            sb_tmp.append(arr.get(0).split("\t")[0] + "\t" + ":" + "\n");

            String session = "NoDebuggingSession";
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).split("\t")[1].equals("Debug.StepOver")
                        || arr.get(i).split("\t")[1].equals("Debug.StopDebugging")
                        || arr.get(i).split("\t")[1].equals("Debug.RunToCursor")
                        || arr.get(i).split("\t")[1].equals("Debug.Breakpoints")) {
                    session = "DebuggingSession";
                }
                sb_tmp.append(arr.get(i));

            }

            sb_tmp.append("\n" + sb_tmp.toString().split("\t")[sb_tmp.toString().split("\t").length - 2].split("\n")[1] + "\t" + "&" + "\n");

            if (session.equals("DebuggingSession")) {
                //add to the full dataset
                sb_tmp_all.append(sb_tmp.toString());
            }

            // PrintWriter for THEME data files   
            PrintWriter pw_theme = null;
            try {
                if (session.equals("DebuggingSession")) {
                    pw_theme = new PrintWriter(new FileOutputStream(new File("theme/events/" + session + debug_count++ + ".txt")/* append = true */));
                } else {
                    pw_theme = new PrintWriter(new FileOutputStream(new File("theme/events/" + session + nodebug_count++ + ".txt")/* append = true */));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            pw_theme.write(sb_tmp.toString().replace('.', '_'));
            pw_theme.close();
        }

        // Print all the data
        try {
            pw_theme_all = new PrintWriter(new FileOutputStream(new File("theme/events/" + "DebuggingAll" + ".txt")/* append = true */));
            pw_theme_all.write("Time" + "\t" + "Event" + "\n");
            pw_theme_all.write(sb_tmp_all.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Print the available events
        PrintWriter pw_events = null;
        try {
            pw_events = new PrintWriter(new FileOutputStream(new File("theme/vvt.vvt") /* append = true */));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (String key : event_names.keySet()) {
            pw_events.write(key + "\n");
            ArrayList<String> arr = event_names.get(key);
            if (arr.isEmpty()) {
                pw_events.write(" " + key + "\n");
            } else {
                for (String element : arr) {
                    element = element.replace('.', '_');
                    pw_events.write(" " + element + "\n");
                }
            }
        }

        pw_events.close();
    }

    private void processUserZip(String userZip) {
        int numProcessedEvents = 0;
        // open the .zip file ...

        try (IReadingArchive ra = new ReadingArchive(new File(eventsDir, userZip))) {
            // ... and iterate over content. 
            // the iteration will stop after 150000 events to speed things up.
            while (ra.hasNext() && (numProcessedEvents++ < 150000)) {
                /*
                 * within the userZip, each stored event is contained as a single file that
                 * contains the Json representation of a subclass of IDEEvent.
                 */
                //IDEEvent e = ra.getNext(IDEEvent.class);
                String json = ra.getNextPlain();
                // .. and call the deserializer yourself.

                try {
                    IIDEEvent e = JsonUtils.fromJson(json, IIDEEvent.class
                    );

                    // the events can then be processed individually
                    //processEvent(e);
                    process(e, userZip);
                } catch (DateTimeException e) {

                    exceptions += 1;

                } finally {
                    continue;
                }
            }
        }
    }

    public static String previous_event = "";
    public static String previous_IDESessionUUID = "";

    public static int build_succesful = 0;
    public static int build_unsuccesful = 0;

    public static int test_succesful = 0;
    public static int test_failed = 0;

    public static TreeSet<String> available_events = new TreeSet<>();
    public static TreeMap<String, ArrayList<String>> event_names = new TreeMap<>();
    public static PrintWriter pw_tmp;

    private static void process(IIDEEvent event, String user) throws ParseException, FileNotFoundException {
        // once you have access to the instantiated event you can dispatch the
        // type. As the events are not nested, we did not implement the visitor
        // pattern, but resorted to instanceof checks.

        event_names.putIfAbsent("ActivityEvent", new ArrayList<>());
        event_names.putIfAbsent("CommandEvent", new ArrayList<>());
        event_names.putIfAbsent("CompletionEvent", new ArrayList<>());
        event_names.putIfAbsent("InstallEvent", new ArrayList<>());
        event_names.putIfAbsent("TestRunEvent", new ArrayList<>());
        event_names.putIfAbsent("UserProfileEvent", new ArrayList<>());
        event_names.putIfAbsent("VersionControlEvent", new ArrayList<>());
        event_names.putIfAbsent("WindowEvent", new ArrayList<>());
        event_names.putIfAbsent("BuildEvent", new ArrayList<>());
        event_names.putIfAbsent("DebuggerEvent", new ArrayList<>());
        event_names.putIfAbsent("DocumentEvent", new ArrayList<>());
        event_names.putIfAbsent("EditEvent", new ArrayList<>());
        event_names.putIfAbsent("FindEvent", new ArrayList<>());
        event_names.putIfAbsent("IDEStateEvent", new ArrayList<>());
        event_names.putIfAbsent("SolutionEvent", new ArrayList<>());
        event_names.putIfAbsent("UpdateEvent", new ArrayList<>());
        event_names.putIfAbsent("ErrorEvent", new ArrayList<>());
        event_names.putIfAbsent("InfoEvent", new ArrayList<>());
        event_names.putIfAbsent("NavigationEvent", new ArrayList<>());
        event_names.putIfAbsent("SystemEvent", new ArrayList<>());
        ArrayList<String> names = null;

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(new File("events.csv"), true /* append = true */));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

//      ActivityEvent 
        if (event instanceof ActivityEvent) {
            ActivityEvent ae = (ActivityEvent) event;
            String name = new File(user).getName();
            activityEvents += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ae.IDESessionUUID);
            sb.append(",");
            sb.append("ActivityEvent");
            sb.append(",");
            sb.append(ae.TriggeredAt);
            sb.append(",");
            sb.append(ae.Duration.getSeconds());
            sb.append("\n");

            String e = "ActivityEvent";

            // to retrieve the list of events  
            String event_type = "ActivityEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            // add the event
            String current_key = ae.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ae.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ae.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ae.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();

            return;
        }
//      CommandEvent
        if (event instanceof CommandEvent) {
            commandEvents += 1;
            CommandEvent ce = (CommandEvent) event;
            String name = new File(user).getName();
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ce.IDESessionUUID);
            sb.append(",");
            sb.append("CommandEvent");
            sb.append(",");
            sb.append(ce.TriggeredAt);
            sb.append(",");
            sb.append(ce.Duration.getSeconds());
            sb.append("\n");

            String e = ce.CommandId.split(":")[2];

            // chech if it is a debugging command
            if (e.startsWith("Debug.") || e.startsWith("Edit.")
                    || e.startsWith("File.") || e.startsWith("Project.")
                    || e.startsWith("View.") || e.startsWith("TestExplorer.")) {

                // to retrieve the list of events  
                String event_type = "CommandEvent";
                if (!event_names.get(event_type).contains(e)) {
                    names = event_names.get(event_type);
                    names.add(e);
                    event_names.put(event_type, names);
                }

                //add the event
                String current_key = ce.IDESessionUUID;

                if (IDESessionUUIDs.containsKey(current_key)) {
                    ArrayList<String> events = IDESessionUUIDs.get(current_key);
                    if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {

                        events.add("\n" + ce.TriggeredAt.toEpochSecond() + "\t" + e);

                        IDESessionUUIDs.put(ce.IDESessionUUID, events);
                        previous_event = e;
                    }
                } else {
                    ArrayList<String> one_event = new ArrayList<>();

                    //controlling start and end
                    if (e.equals("Debug.Start")) {
                        one_event.add("\n" + ce.TriggeredAt.toEpochSecond() + "\t" + "b");
                    } else if (e.equals("Debug.StopDebugging")) {
                        one_event.add("\n" + ce.TriggeredAt.toEpochSecond() + "\t" + "e");
                    } else {
                        one_event.add("\n" + ce.TriggeredAt.toEpochSecond() + "\t" + e);
                    }
                    IDESessionUUIDs.put(current_key, one_event);
                    previous_event = e;
                }
            }

            pw.write(sb.toString());
            pw.close();

            return;
        }
//      CompletionEvent 
        if (event instanceof CompletionEvent) {
            CompletionEvent ce = (CompletionEvent) event;
            String name = new File(user).getName();
            completionevents += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ce.IDESessionUUID);
            sb.append(",");
            sb.append("CompletionEvent");
            sb.append(",");
            sb.append(ce.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "CompletionEvent." + ce.terminatedState.name();

            // to retrieve the list of events  
            String event_type = "CompletionEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ce.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ce.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ce.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ce.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

//      InstallEvent  
        if (event instanceof InstallEvent) {
            InstallEvent ie = (InstallEvent) event;
            String name = new File(user).getName();
            installevent += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ie.IDESessionUUID);
            sb.append(",");
            sb.append("InstallEvent");
            sb.append(",");
            sb.append(ie.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "InstallEvent";

            // to retrieve the list of events  
            String event_type = "InstallEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ie.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ie.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ie.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ie.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

//      TestRunEvent 
        if (event instanceof TestRunEvent) {
            TestRunEvent tre = (TestRunEvent) event;
            String name = new File(user).getName();
            testse += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(tre.IDESessionUUID);
            sb.append(",");
            sb.append("TestRunEvent");
            sb.append(",");
            sb.append(tre.TriggeredAt);
            sb.append(",");
            sb.append(tre.Duration.getSeconds());
            sb.append("\n");

            boolean Successful = true;
            String testResult = "";
            for (TestCaseResult result : tre.Tests) {
                if (!result.Result.name().equals("Success")) {
                    Successful = false;
                    break;
                }
            }
            if (Successful) {
                test_succesful++;
                testResult = "Successful";
            } else {
                test_failed++;
                testResult = "Failed";
            }

            String e = "TestRunEvent." + testResult;

            // to retrieve the list of events  
            String event_type = "TestRunEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = tre.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + tre.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(tre.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(tre.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

//      UserProfileEvent
        if (event instanceof UserProfileEvent) {
            UserProfileEvent upe = (UserProfileEvent) event;
            String name = new File(user).getName();
            userprofile += 1;
            sb.append(new File(user).getParent() + "/" + name);
            sb.append(",");
            sb.append(upe.IDESessionUUID);
            sb.append(",");
            sb.append("UserProfileEvent");
            sb.append(",");
            sb.append(upe.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "UserProfileEvent";

            // to retrieve the list of events  
            String event_type = "UserProfileEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = upe.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + upe.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(upe.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(upe.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();

            if (!ids.containsKey(upe.ProfileId)) {
                ArrayList<String> files = new ArrayList<String>();
                files.add(new File(user).getParent() + "/" + name);
                ids.put(upe.ProfileId, files);
                return;
            } else if (!ids.get(upe.ProfileId).contains(new File(user).getParent() + "/" + name)) {
                ids.get(upe.ProfileId).add(new File(user).getParent() + "/" + name);
            }
            return;
        }

//      VersionControlEvent  
        if (event instanceof VersionControlEvent) {
            VersionControlEvent vce = (VersionControlEvent) event;
            String name = new File(user).getName();
            versione += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(vce.IDESessionUUID);
            sb.append(",");
            sb.append("VersionControlEvent");
            sb.append(",");
            sb.append(vce.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            List<VersionControlAction> actions = vce.Actions;

            for (VersionControlAction action : actions) {
                String e = "VersionControlEvent." + action.ActionType.name();

                // to retrieve the list of events  
                String event_type = "VersionControlEvent";
                if (!event_names.get(event_type).contains(e)) {
                    names = event_names.get(event_type);
                    names.add(e);
                    event_names.put(event_type, names);
                }

                //add the event
                String current_key = vce.IDESessionUUID;
                if (IDESessionUUIDs.containsKey(current_key)) {
                    ArrayList<String> events = IDESessionUUIDs.get(current_key);
                    if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                        events.add("\n" + vce.TriggeredAt.toEpochSecond() + "\t" + e);
                        IDESessionUUIDs.put(vce.IDESessionUUID, events);
                        previous_event = e;
                    }
                } else {
                    ArrayList<String> one_event = new ArrayList<>();
                    one_event.add(vce.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(current_key, one_event);
                    previous_event = e;
                }

            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

//      WindowEvent
        if (event instanceof WindowEvent) {
            WindowEvent we = (WindowEvent) event;
            String name = new File(user).getName();
            windowe += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(we.IDESessionUUID);
            sb.append(",");
            sb.append("WindowEvent");
            sb.append(",");
            sb.append(we.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "WindowEvent." + we.Action.name();

            // to retrieve the list of events
            String event_type = "WindowEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = we.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + we.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(we.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(we.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

//      BuildEvent  
        if (event instanceof BuildEvent) {
            BuildEvent be = (BuildEvent) event;
            String name = new File(user).getName();
            builde += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(be.IDESessionUUID);
            sb.append(",");
            sb.append("BuildEvent");
            sb.append(",");
            sb.append(be.TriggeredAt);
            sb.append(",");
            sb.append(be.Duration.getSeconds());
            sb.append("\n");

            boolean Successful = true;
            String buildResult = "";
            for (BuildTarget bt : be.Targets) {
                if (!bt.Successful) {
                    Successful = false;
                    break;
                }
            }
            if (Successful) {
                build_succesful++;
                buildResult = "Successful";
            } else {
                build_unsuccesful++;
                buildResult = "Unsuccessful";
            }

            String e = "BuildEvent." + buildResult;

            // to retrieve the list of events
            String event_type = "BuildEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = be.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + be.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(be.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(be.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //DebuggerEvent
        if (event instanceof DebuggerEvent) {
            DebuggerEvent de = (DebuggerEvent) event;
            String name = new File(user).getName();
            debuggere += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(de.IDESessionUUID);
            sb.append(",");
            sb.append("DebuggerEvent");
            sb.append(",");
            sb.append(de.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = de.Mode.name() + "." + de.Reason.replace(".", "_");

            // to retrieve the list of events
            String event_type = "DebuggerEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = de.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + de.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(de.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(de.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        } //

//       DocumentEvent
        if (event instanceof DocumentEvent) {
            DocumentEvent doc = (DocumentEvent) event;
            String name = new File(user).getName();
            documente += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(doc.IDESessionUUID);
            sb.append(",");
            sb.append("DocumentEvent");
            sb.append(",");
            sb.append(doc.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "DocumentEvent." + doc.Action.name();

            // to retrieve the list of events
            String event_type = "DocumentEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = doc.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + doc.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(doc.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(doc.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //EditEvent
        if (event instanceof EditEvent) {
            EditEvent ee = (EditEvent) event;
            String name = new File(user).getName();
            String u_name = name.substring(0, name.lastIndexOf(".zip"));
            editevent += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ee.IDESessionUUID);
            sb.append(",");
            sb.append("EditEvent");
            sb.append(",");
            sb.append(ee.TriggeredAt);
            sb.append(",");
            sb.append(ee.Duration.getSeconds());
            sb.append("\n");

            String e = "EditEvent";

            //            pw_tmp.write(ee.SizeOfChanges + "\n");
            // to retrieve the list of events
            String event_type = "EditEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);

                if (ee.SizeOfChanges < 30) {
                    names.add(e + ".Short");
                    event_names.put(event_type, names);
                } else {
                    names.add(e + ".Large");
                    event_names.put(event_type, names);
                }

            }

            //add the event
            String current_key = ee.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {

                    if (ee.SizeOfChanges < 30) {
                        events.add("\n" + ee.TriggeredAt.toEpochSecond() + "\t" + e + ".Short");
                    } else {
                        events.add("\n" + ee.TriggeredAt.toEpochSecond() + "\t" + e + ".Large");
                    }

                    IDESessionUUIDs.put(ee.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ee.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }
        //      FindEvent  
        if (event instanceof FindEvent) {
            FindEvent fe = (FindEvent) event;
            String name = new File(user).getName();
            String u_name = name.substring(0, name.lastIndexOf(".zip"));
            finde += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(fe.IDESessionUUID);
            sb.append(",");
            sb.append("FindEvent");
            sb.append(",");
            sb.append(fe.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String tmp = fe.Cancelled ? "Cancelled" : "NotCancelled";
            String e = "FindEvent." + tmp;

            // to retrieve the list of events
            String event_type = "FindEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = fe.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + fe.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(fe.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(fe.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

//      IDEStateEvent  
        if (event instanceof IDEStateEvent) {
            IDEStateEvent ide = (IDEStateEvent) event;
            String name = new File(user).getName();
            String u_name = name.substring(0, name.lastIndexOf(".zip"));
            ideee += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ide.IDESessionUUID);
            sb.append(",");
            sb.append("IDEStateEvent");
            sb.append(",");
            sb.append(ide.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "IDEStateEvent." + ide.IDELifecyclePhase.name();

            // to retrieve the list of events
            String event_type = "IDEStateEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ide.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ide.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ide.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ide.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //      SolutionEvent  
        if (event instanceof SolutionEvent) {
            SolutionEvent se = (SolutionEvent) event;
            String name = new File(user).getName();
            String u_name = name.substring(0, name.lastIndexOf(".zip"));
            solutionevent += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(se.IDESessionUUID);
            sb.append(",");
            sb.append("SolutionEvent");
            sb.append(",");
            sb.append(se.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "SolutionEvent." + se.Action.name();

            // to retrieve the list of events
            String event_type = "SolutionEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = se.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + se.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(se.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(se.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //      UpdateEvent
        if (event instanceof UpdateEvent) {
            UpdateEvent ue = (UpdateEvent) event;
            String name = new File(user).getName();
            updatevent += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ue.IDESessionUUID);
            sb.append(",");
            sb.append("UpdateEvent");
            sb.append(",");
            sb.append(ue.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "UpdateEvent";

            // to retrieve the list of events
            String event_type = "UpdateEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ue.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ue.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ue.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ue.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //      ErrorEvent  
        if (event instanceof ErrorEvent) {
            ErrorEvent ee = (ErrorEvent) event;
            String name = new File(user).getName();
            errore += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ee.IDESessionUUID);
            sb.append(",");
            sb.append("ErrorEvent");
            sb.append(",");
            sb.append(ee.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "ErrorEvent";

            // to retrieve the list of events
            String event_type = "ErrorEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ee.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ee.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ee.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ee.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //      InfoEvent
        if (event instanceof InfoEvent) {
            InfoEvent ie = (InfoEvent) event;
            String name = new File(user).getName();
            infoe += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ie.IDESessionUUID);
            sb.append(",");
            sb.append("InfoEvent");
            sb.append(",");
            sb.append(ie.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "InfoEvent";

            // to retrieve the list of events
            String event_type = "InfoEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ie.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ie.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ie.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ie.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //      NavigationEvent  
        if (event instanceof NavigationEvent) {
            NavigationEvent ne = (NavigationEvent) event;
            String name = new File(user).getName();
            navigationevents += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(ne.IDESessionUUID);
            sb.append(",");
            sb.append("NavigationEvent");
            sb.append(",");
            sb.append(ne.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "NavigationEvent." + ne.TypeOfNavigation.name();

            // to retrieve the list of events
            String event_type = "NavigationEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = ne.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + ne.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(ne.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(ne.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        }

        //      SystemEvent
        if (event instanceof SystemEvent) {
            SystemEvent se = (SystemEvent) event;
            String name = new File(user).getName();
            String u_name = name.substring(0, name.lastIndexOf(".zip"));
            systemevents += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append(se.IDESessionUUID);
            sb.append(",");
            sb.append("SystemEvent");
            sb.append(",");
            sb.append(se.TriggeredAt);
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            String e = "SystemEvent." + se.Type.name();

            // to retrieve the list of events
            String event_type = "SystemEvent";
            if (!event_names.get(event_type).contains(e)) {
                names = event_names.get(event_type);
                names.add(e);
                event_names.put(event_type, names);
            }

            //add the event
            String current_key = se.IDESessionUUID;
            if (IDESessionUUIDs.containsKey(current_key)) {
                ArrayList<String> events = IDESessionUUIDs.get(current_key);
                if (!events.get(events.size() - 1).split("\t")[1].equals(e) && !previous_event.equals(e)) {
                    events.add("\n" + se.TriggeredAt.toEpochSecond() + "\t" + e);
                    IDESessionUUIDs.put(se.IDESessionUUID, events);
                    previous_event = e;
                }
            } else {
                ArrayList<String> one_event = new ArrayList<>();
                one_event.add(se.TriggeredAt.toEpochSecond() + "\t" + e);
                IDESessionUUIDs.put(current_key, one_event);
                previous_event = e;
            }

            pw.write(sb.toString());
            pw.close();
            return;
        } else {
            // there a many different event types to process, it is recommended
            // that you browse the package to see all types and consult the
            // website for the documentation of the semantics of each event...
            String name = new File(user).getName();
            other += 1;
            sb.append(sb.append(new File(user).getParent() + "/" + name));
            sb.append(",");
            sb.append("N/A");
            sb.append(",");
            sb.append(event.getClass().getSimpleName());
            sb.append(",");
            sb.append(event.getTriggeredAt());
            sb.append(",");
            sb.append("N/A");
            sb.append("\n");

            pw.write(sb.toString());
            pw.close();
        }
    }

}
