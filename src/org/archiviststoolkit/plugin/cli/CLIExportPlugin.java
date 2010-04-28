package org.archiviststoolkit.plugin.cli;

import org.archiviststoolkit.ApplicationFrame;
import org.archiviststoolkit.util.StringHelper;
import org.archiviststoolkit.exporter.EADExport;
import org.archiviststoolkit.editor.ArchDescriptionFields;
import org.archiviststoolkit.model.Resources;
import org.archiviststoolkit.mydomain.DomainAccessObject;
import org.archiviststoolkit.mydomain.DomainEditorFields;
import org.archiviststoolkit.mydomain.DomainObject;
import org.archiviststoolkit.mydomain.ResourcesDAO;
import org.archiviststoolkit.plugin.ATPlugin;
import org.archiviststoolkit.swing.InfiniteProgressPanel;
import org.java.plugin.Plugin;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/* Based off the cli plugin code written by the person below 
 * Currently only supports EAD export of resources that have
 * a finding aid status of "Complete"
 * */

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: Apr 19, 2010
 * Time: 8:01:58 PM
 *
 * This is a sample plugin which demonstrate how to develope
 * Command Line Interface, CLI, plugins for the AT. It also demonstrate
 * techniques for saving memory which is critical when batch processing
 * large amount of records.
 */
public class CLIExportPlugin extends Plugin implements ATPlugin {
    /**
     *  get the category(ies) this plugin belongs to. For plugins that
     *  are only used through the CLI then the below is fine. Additional
     *  categories can be defined if this plugin is also to be used with
     *  the main AT program
     */
    public String getCategory() {
        //return ATPlugin.CLI_CATEGORY + " " + ATPlugin.DEFAULT_CATEGORY;
        return ATPlugin.CLI_CATEGORY;
    }

    // get the name of this plugin
    public String getName() {
        return "CLI Export";
    }

    /**
     * Method to actually execute the logic of the plugin. It is
     * automatically called by the atcli program so it needs to be
     * implemented
     *
     * @param task The task that is passed in as the second argument in
     * the command line parameters
     */
    public void doTask(String task) {
        // show the command line parameters
        String[] params = org.archiviststoolkit.plugin.ATPluginFactory.getInstance().getCliParameters();
        for(int i = 0; i < params.length; i++) {
            System.out.println("Parameter " + (i+1) + " = " + params[i]);
        }

        /*
        * do a test by getting all the resource rocords and print name out
        * their name title and export as EAD. Thanks for Cyrus Farajpour
        * for providing some of this code
        */

        DomainAccessObject access = new ResourcesDAO();

        // get the list of all resources from the database
        java.util.List<Resources> resources;
        java.util.List<Resources> completedResources;

        try {
            resources = (java.util.List<Resources>) access.findAll();
        } catch (Exception e) {
            resources = null;
        }

        int recordCount = resources.size();
        
        // Get the path where to export the files to from the command line arguments
        String exportRootPath = params[2];

        //dummy progress panel to pass into convertResourceToFile
        InfiniteProgressPanel fakePanel = new InfiniteProgressPanel();

        // print out the resource identifier and title and export as ead
        System.out.println("Exporting EADs ");

        // Get the runtime for clearing memory
        Runtime runtime = Runtime.getRuntime();

        // get the ead exporter
        EADExport ead = new EADExport();

        for(int i = 0; i < recordCount; i++) {
        	if(resources.get(i).getFindingAidStatus().equals("Completed")){
	            try {
	                // load the full resource from database using a long session
	                Resources resource = (Resources)access.findByPrimaryKeyLongSession(resources.get(i).getIdentifier());
	
	                System.out.println(resource.getResourceIdentifier() + " : " + resource.getTitle());
	
	                String fileName = StringHelper.removeInvalidFileNameCharacters(resource.getResourceIdentifier());
	                java.io.File file = new java.io.File(exportRootPath + fileName + ".xml");
	                file.createNewFile();
	                ead.convertResourceToFile(resource, file, fakePanel, false, true, true, true);
	
	                
	            } catch (Exception e) {
	                System.out.println(e);
	            }
	
	            // close the long session. This is critical to saving memory. If it's
	            // left open then hinernate caches the resource records even though
	            // we don't need them anymore
	            access.getLongSession().close(); // close the connection
	
	            
        	}
        	// since we no longer need this resource, set it to null
            // close the session in an attemp to save memory
            resources.set(i, null);
            
            // run GC to clear some memory after 10 exports, not sure if this is
            // really needed but running GC cost little in time so might as well?
            runtime.gc();
        }

        System.out.println("Finished Exporting ...");
    }

    /**
     * Method to get the list of specific task the plugin can perform. Only
     * the task at position 0 in the string array is checked in CLI plugins
     *
     * @return String array containing the task(s) the plugin is registered
     * to handel.
     */
    public String[] getTaskList() {
        String[] tasks = {"ead"};

        return tasks;
    }

    
    /*
     *
     * Method below this point do not need to implemented in a command line plugin,
     * but can be for those that also have GUIs, for example for configuration.
     *
     */

    public void setApplicationFrame(ApplicationFrame mainFrame) { }

    public void showPlugin() { }

    public void showPlugin(Frame owner) { }

    public void showPlugin(Dialog owner) { }

    public HashMap getEmbeddedPanels() { return null; }

    public void setEditorField(ArchDescriptionFields editorField) { }

    public void setEditorField(DomainEditorFields domainEditorFields) { }

    public void setModel(DomainObject domainObject, InfiniteProgressPanel monitor) { }

    public void setCallingTable(JTable callingTable) { }

    public void setSelectedRow(int selectedRow) { }

    public void setRecordPositionText(int recordNumber, int totalRecords) { }

    public String getEditorType() {return null; }

    protected void doStart() { }

    protected void doStop() { }
}