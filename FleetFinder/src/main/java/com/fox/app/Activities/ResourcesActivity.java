package com.fox.app.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.avery.sampleapp.R;

import java.io.File;
import java.util.Arrays;

import avd.api.core.IDevice;
import avd.api.core.IPrinter;
import avd.api.core.exceptions.ApiPrinterException;
import avd.api.core.imports.ResourceMediaType;
import avd.api.resources.ApiResourceException;
import avd.api.resources.ResourceManager;

import com.fox.app.SampleApplication;
import com.fox.app.filechooser.Constants;
import com.fox.app.filechooser.FileChooserActivity;

public class ResourcesActivity extends SampleAppActivity {

    private EditText resourcePathEditText = null;
    private static int resourceTypeSelected = R.id.rbResourceTypeAll;
    private SampleApplication application = null;
    private ArrayAdapter<String> presentResourcesArrayAdapter;
    private ListView presentResourcesListView = null;
    private String resourceToCheck = null;
    private String addResourceQuery = null;
    private String            resourceToAddPath;
    private String            resourceToAddAlias;
    private ResourceMediaType resourceToAddType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        application = ((SampleApplication) getApplication());
        this.setupDefaults();
    }

    @Override
    protected void onResume() {
        super.onResume();

        application.requiredDevice = application.NO_DEVICES;
        application.lastActivityForAnyDevice = ResourcesActivity.class;

        // We are showing message box in onResume instead of in onActivityResult, because if we call it in onActivityResult
        // onResume will be called after it and Sample Application instance would duplicate the message box via restoreAllAlerts method.
        if (addResourceQuery != null)
        {
            if (resourceToAddType == null)
                showMessageBox("Adding resource", addResourceQuery, "Ok", null, null, null, null);
            else
                showMessageBox("Adding resource", addResourceQuery, "Ok", addResourceClickListener, "Cancel", null, null);

            addResourceQuery = null;
        }

    }

    public void setupDefaults()
    {
        application.initializeDeviceIndexButtons(this);

        resourcePathEditText = (EditText) findViewById(R.id.etResourcePath);
        resourcePathEditText.setSingleLine();
        resourcePathEditText.setText(application.resourcePath);
        resourcePathEditText.setEnabled(true);
        resourcePathEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // First pad the directory path with the / sign, if needed
                    if (!resourcePathEditText.getText().toString().endsWith("/"))
                        resourcePathEditText.setText(resourcePathEditText.getText().toString() + "/");

                    showMessageBox("Initialize resource path",
                            "Do you really want to set the new value of the resource path to " + resourcePathEditText.getText() + "?",
                            "Confirm", setResourcePathListener, "Cancel", restoreResourcePath, null);

                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(resourcePathEditText.getWindowToken(), 0);
                    resourcePathEditText.clearFocus();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });

        presentResourcesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
        presentResourcesListView = (ListView) findViewById(R.id.available_resources);
        presentResourcesListView.setAdapter(presentResourcesArrayAdapter);
        presentResourcesListView.setOnItemClickListener(resourceClickListener);

        RadioGroup rgSupplyType = (RadioGroup) findViewById(R.id.rgResourceType);
        rgSupplyType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                resourceTypeSelected = checkedId;
                refreshDisplayedResources();
            }
        });
        rgSupplyType.check(resourceTypeSelected);

        Button btnAddResource = (Button) findViewById(R.id.btnAddResource);
        btnAddResource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.isButtonPressAllowed)
                    return;

                resourceToAddPath = null;
                resourceToAddAlias = null;
                resourceToAddType = null;

                Intent intent = new Intent(ResourcesActivity.this, FileChooserActivity.class);
                startActivityForResult(intent, SampleApplication.INTENT_FILE_CHOOSER_ID);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == SampleApplication.INTENT_FILE_CHOOSER_ID) && (resultCode == RESULT_OK)) {
            resourceToAddPath = data.getStringExtra(Constants.KEY_FILE_SELECTED);
            int extensionIndex = resourceToAddPath.lastIndexOf('.');
            String extension = null;
            if (extensionIndex != -1)
                extension = resourceToAddPath.substring(extensionIndex + 1).toUpperCase();

            if ("LNT".equals(extension) || "XML".equals(extension) || "JOB".equals(extension)) {
                resourceToAddType = ResourceMediaType.Lnt;
                addResourceQuery = "You have selected the LNT " + resourceToAddPath + "\nDo you want to add it to the list of the resources?";
            } else if ("TTF".equals(extension) || "FON".equals(extension) || "FNT".equals(extension)) {
                resourceToAddType = ResourceMediaType.Font;
                addResourceQuery = "You have selected the Font File " + resourceToAddPath + "\nDo you want to add it to the list of the resources?";
            } else if ("PNG".equals(extension) || "BMP".equals(extension) || "JPG".equals(extension) || "JPEG".equals(extension) || "GIF".equals(extension) || "TIF".equals(extension) || "TIFF".equals(extension)) {
                resourceToAddType = ResourceMediaType.Graphic;
                addResourceQuery =  "You have selected the Graphic File " + resourceToAddPath + "\nDo you want to add it to the list of the resources?";
            } else
                addResourceQuery = "You have selected the improper file " + resourceToAddPath + "\nThis file is not recognized as a proper resource file.";
        }
    }

    private DialogInterface.OnClickListener setResourcePathListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            File newResourcePath = new File(resourcePathEditText.getText().toString());
            if (!newResourcePath.exists())
                newResourcePath.mkdirs();

            if (!newResourcePath.isDirectory())
            {
                showMessageBox("Initialize resource path fail",
                        "You cannot set resource path to new value because " + resourcePathEditText.getText() + " is not a valid directory.",
                        "Ok", null, null, null, null);
                resourcePathEditText.setText(application.resourcePath);
                return;
            }

            if (!newResourcePath.canWrite())
            {
                showMessageBox("Initialize resource path fail",
                        "You cannot set resource path to new value because " + resourcePathEditText.getText() + " is not a writable directory.",
                        "Ok", null, null, null, null);
                resourcePathEditText.setText(application.resourcePath);
                return;
            }

            try {
                ResourceManager.initializeResourcePath(newResourcePath.toString());
                application.resourcePath = newResourcePath.toString();
                resourcePathEditText.setText(application.resourcePath);
            } catch (ApiResourceException e) {
                showNonAbortableErrorMessageBox("Initializing new resource path failed", e, null);
            }

            refreshDisplayedResources();
        }
    };

    private DialogInterface.OnClickListener restoreResourcePath = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            resourcePathEditText.setText(application.resourcePath);
        }
    };

    private DialogInterface.OnClickListener removeCheckedResourceListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            removeCheckedResource(true);
        }
    };

    private DialogInterface.OnClickListener printCheckedResourceListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            byte[][] emptyString = {"".getBytes()};
            if (application.allDevicesSelected()) {
                for (SampleApplication.DeviceData deviceData : application.connectedDevicesData.values()) {
                    IDevice device = deviceData.device;
                    IPrinter printer = device.getPrinter();
                    try {
                        // We set the quantity to 0 so that element "Quantity" from the LNT is used.
                        printer.print(resourceToCheck, 0, emptyString);
                    } catch (ApiPrinterException e) {
                        showStandardErrorMessageBox("Lnt print failed", e, device);
                    }
                }
            }
            else {
                IDevice device = application.getDevice();
                if (device == null) {
                    showMessageBox("Error!",
                            "No device connected. Please connect first.",
                            "Ok", null, null, null, null);
                    return;
                }

                IPrinter printer = device.getPrinter();
                try {
                    // We set the quantity to 0 so that element "Quantity" from the LNT is used.
                    printer.print(resourceToCheck, 0, emptyString);
                } catch (ApiPrinterException e) {
                    showStandardErrorMessageBox("Lnt print failed", e, device);
                }
            }
        }
    };

    private DialogInterface.OnClickListener addResourceClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ResourcesActivity.this);
            final EditText etResourceAlias = new EditText(ResourcesActivity.this);
            etResourceAlias.setHint("Please enter the resource alias.");
            dialogBuilder.setTitle("Setting resource alias")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(etResourceAlias)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            resourceToAddAlias = etResourceAlias.getText().toString();
                            addResource();
                        }
                    })
                    .setNegativeButton("Cancel", null);

            AlertDialog newDialog = dialogBuilder.create();
            newDialog.setCanceledOnTouchOutside(false);
            newDialog.show();
        }
    };

    private AdapterView.OnItemClickListener resourceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            CheckedTextView ctv = (CheckedTextView) v;
            resourceToCheck = ctv.getText().toString();
            presentResourcesListView.setItemChecked(arg2, false);

            boolean isResourcePresent;
            if (getResourceTypeFromId() == ResourceMediaType.All)
                isResourcePresent = ResourceManager.checkResource(ResourceMediaType.Font, resourceToCheck) ||
                                    ResourceManager.checkResource(ResourceMediaType.Graphic, resourceToCheck) ||
                                    ResourceManager.checkResource(ResourceMediaType.Lnt, resourceToCheck);
            else
                isResourcePresent = ResourceManager.checkResource(getResourceTypeFromId(), resourceToCheck);

            if (isResourcePresent)
            {
                if (getResourceTypeFromId() == ResourceMediaType.All)
                    showMessageBox("Resource check",
                            "Resource " + resourceToCheck + " is present in the system.",
                            "Ok", null, null, null, null);
                else if (getResourceTypeFromId() == ResourceMediaType.Lnt)
                    createAndShowMessageBox("Resource check",
                                            "LNT document " + resourceToCheck + " is present in the system.\nDo you want to print a label with it, delete it, or ignore?",
                                            "Delete", removeCheckedResourceListener,
                                            "Print", printCheckedResourceListener,
                                            "Ignore", null,
                                            null);
                else
                    showMessageBox("Resource check",
                                           "Resource " + resourceToCheck + " is present in the system.\nDo you want to delete it?",
                                           "Delete", removeCheckedResourceListener,
                                           "Cancel", null, null);
            }
            else
            {
                showMessageBox("Resource check",
                        "Resource " + resourceToCheck + " is not present in the system. Deleting it.",
                        "Ok", null, null, null, null);
                removeCheckedResource(false);
            }

        }
    };

    private void refreshDisplayedResources()
    {
        ResourceMediaType resourceTypeID = getResourceTypeFromId();

        try {
            String[] presentResources = ResourceManager.getResourceList(resourceTypeID);

            presentResourcesArrayAdapter.clear();
            if (presentResources.length > 0) {
                presentResourcesListView.setEnabled(true);
                for (int i = 0; i < presentResources.length; ++i)
                    presentResourcesArrayAdapter.add(presentResources[i]);
            } else {
                String noResources = "No resources present";//getResources().getText(R.string.none_paired).toString();
                presentResourcesArrayAdapter.add(noResources);
                presentResourcesListView.setEnabled(false);
            }
            presentResourcesArrayAdapter.notifyDataSetChanged();
        } catch (ApiResourceException e) {
            showNonAbortableErrorMessageBox("Resource obtaining fail", e, null);
        }
    }

    private void addResource()
    {
        if ((resourceToAddPath == null) || (resourceToAddAlias == null) || resourceToAddAlias.isEmpty() || (resourceToAddType == null))
        {
            showMessageBox("Adding resource", "Cannot add resource. Some of the data is missing.", "Ok", null, null, null, null);
            return;
        }

        try {
            if (Arrays.asList(ResourceManager.getResourceList(resourceToAddType)).contains(resourceToAddAlias))
            {
                showMessageBox("Adding resource", "Alias " + resourceToAddAlias + " already taken. Please select resource again and use another alias.", "Ok", null, null, null, null);
                return;
            }
            ResourceManager.registerResource(resourceToAddType, resourceToAddAlias, resourceToAddPath);
        } catch (ApiResourceException e) {
            showErrorMessageBox("Adding resource", e, "Ok", null, null, null, null);
        }

        refreshDisplayedResources();
    }


    private void removeCheckedResource(boolean unregisterResource)
    {
        if (resourceToCheck == null)
            return;

        if (unregisterResource)
        {
            try {
                ResourceManager.unregisterResource(getResourceTypeFromId(), resourceToCheck);
            } catch (ApiResourceException e) {
                showNonAbortableErrorMessageBox("Unregistering resource failed", e, null);
            }
        }

        presentResourcesArrayAdapter.remove(resourceToCheck);
        presentResourcesArrayAdapter.notifyDataSetChanged();
    }

    private ResourceMediaType getResourceTypeFromId()
    {
        ResourceMediaType result;
        switch (resourceTypeSelected) {
            case R.id.rbResourceTypeFont: {
                result = ResourceMediaType.Font;
                break;
            }
            case R.id.rbResourceTypeGraphic: {
                result = ResourceMediaType.Graphic;
                break;
            }
            case R.id.rbResourceTypeLnt: {
                result = ResourceMediaType.Lnt;
                break;
            }
            case R.id.rbResourceTypeAll:
            default: {
                result = ResourceMediaType.All;
                break;
            }
        }

        return result;
    }
}
