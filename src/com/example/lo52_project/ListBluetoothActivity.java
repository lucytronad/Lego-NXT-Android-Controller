package com.example.lo52_project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;


/**
 * ListBluetooth Actvity
 * Allow to display all bluetooth device found by bluetooth discovering
 * Allow to select a bluetooth device and send its name and mac address to the main activity 
 * @author lois Aubree & lucie Boutou
 *
 */
public class ListBluetoothActivity extends ListActivity 
{
	
	private List<HashMap<String, String>> mListhDevice = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter mListAdapter = null;
	private static final String NXT_NAME = "nxt_name" ;
	private static final String NXT_ADD = "nxt_address";
	
	/**
	 * Add a founded device to the list of device 
	 * Each device is adding to the list with name and address keys
	 */
	private final BroadcastReceiver DeviceFoundReceiver = new BroadcastReceiver(){

		  @Override
		  public void onReceive(Context context, Intent intent) {
	
			  String action = intent.getAction();
			  if(BluetoothDevice.ACTION_FOUND.equals(action))
			  {
				  BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				  HashMap<String, String> hDevice = new HashMap<String, String>();
				  hDevice.put("name", device.getName());
				  hDevice.put("address", device.getAddress());
				  mListhDevice.add(hDevice);
				  mListAdapter.notifyDataSetChanged();  
			  }
		  }};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * Adapter to display bluetooth device with to keys name and address
		 */
		mListAdapter = new SimpleAdapter(this,mListhDevice,
				R.layout.list_view,
				new String[]{"name","address"},
				new int[]{R.id.textNameDevice,R.id.textAddDevice});
		
		registerReceiver(DeviceFoundReceiver,
				new IntentFilter(BluetoothDevice.ACTION_FOUND));
		
		setListAdapter(mListAdapter);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(DeviceFoundReceiver);
		super.onDestroy();
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_menu, menu);
		return true;
	}
	
	/**
	 * Allows to run the bluetooth discovery once again 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.rescan_bluetooth)
			BluetoothAdapter.getDefaultAdapter().startDiscovery();
		return true;
	};
	
	/**
	 * Select the device and return the name and mac address to the activity that 
	 * started the bluetoothListActivity
	 * finally, finish this activity to go back to the main activity
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		HashMap<String, String> device = mListhDevice.get(position);
		String name = device.get("name");
		String address = device.get("address");
		
		Intent intent = new Intent();
		intent.putExtra(NXT_NAME, name);
		intent.putExtra(NXT_ADD, address);
		
		setResult(RESULT_OK,intent);
		
		finish();
	
	}

}
