package com.idevicesinc.sweetblue;

import android.bluetooth.BluetoothGattService;

import com.idevicesinc.sweetblue.utils.Utils;

import java.util.UUID;

/**
 * Proxy of {@link BluetoothGattService} to force stricter compile-time checks and order of operations
 * when creating services for {@link BleServer}.
 */
public class BleService
{
	private final BluetoothGattService m_native;

	public BleService(final UUID uuid, final BleCharacteristic... characteristics)
	{
		this(uuid, true, characteristics);
	}

	private BleService(final UUID uuid, final boolean constructorOverloadEnabler2000, final BleCharacteristic[] characteristics)
	{
		final int serviceType = BluetoothGattService.SERVICE_TYPE_PRIMARY;

		m_native = new BluetoothGattService(uuid, serviceType);

		for( int i = 0; i < characteristics.length; i++ )
		{
			m_native.addCharacteristic(characteristics[i].m_native);
		}
	}
}
