package com.idevicesinc.sweetblue;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.idevicesinc.sweetblue.utils.Pointer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;


@Config(manifest = Config.NONE, sdk = 24)
@RunWith(RobolectricTestRunner.class)
public final class DefaultTransactionsTest extends BaseBleUnitTest
{


    private final static UUID mAuthServiceUuid = UUID.randomUUID();
    private final static UUID mAuthCharUuid = UUID.randomUUID();
    private final static UUID mInitServiceUuid = UUID.randomUUID();
    private final static UUID mInitCharUuid = UUID.randomUUID();


    @Test(timeout = 10000)
    public void defaultAuthTransactionTest() throws Exception
    {
        m_config.runOnMainThread = false;
        m_config.defaultScanFilter = new BleManagerConfig.ScanFilter()
        {
            @Override public Please onEvent(ScanEvent e)
            {
                return Please.acknowledgeIf(e.name_native().contains("Test Device"));
            }
        };
        m_config.defaultAuthFactory = new BleDeviceConfig.AuthTransactionFactory()
        {
            @Override public BleTransaction.Auth newAuthTxn()
            {
                return new BleTransaction.Auth()
                {
                    @Override protected void start(BleDevice device)
                    {
                        device.read(mAuthServiceUuid, mAuthCharUuid, new BleDevice.ReadWriteListener()
                        {
                            @Override public void onEvent(ReadWriteEvent e)
                            {
                                succeed();
                            }
                        });
                    }
                };
            }
        };

        m_config.loggingEnabled = true;

        connectToMultipleDevices(m_config);

        m_mgr.stopScan();
        m_mgr.disconnectAll();

        m_config.runOnMainThread = true;

        connectToMultipleDevices(m_config);
    }

    @Test(timeout = 10000)
    public void defaultInitTransactionTest() throws Exception
    {
        m_config.runOnMainThread = false;
        m_config.defaultScanFilter = new BleManagerConfig.ScanFilter()
        {
            @Override public Please onEvent(ScanEvent e)
            {
                return Please.acknowledgeIf(e.name_native().contains("Test Device"));
            }
        };
        m_config.defaultInitFactory = new BleDeviceConfig.InitTransactionFactory()
        {
            @Override public BleTransaction.Init newInitTxn()
            {
                return new BleTransaction.Init()
                {
                    @Override protected void start(BleDevice device)
                    {
                        device.read(mInitServiceUuid, mInitCharUuid, new BleDevice.ReadWriteListener()
                        {
                            @Override public void onEvent(ReadWriteEvent e)
                            {
                                succeed();
                            }
                        });
                    }
                };
            }
        };

        m_config.loggingEnabled = true;

        connectToMultipleDevices(m_config);

        m_mgr.stopScan();
        m_mgr.disconnectAll();

        m_config.runOnMainThread = true;

        connectToMultipleDevices(m_config);
    }

    @Test(timeout = 10000)
    public void defaultAuthAndInitTransactionTest() throws Exception
    {
        m_config.runOnMainThread = false;
        m_config.defaultScanFilter = new BleManagerConfig.ScanFilter()
        {
            @Override public Please onEvent(ScanEvent e)
            {
                return Please.acknowledgeIf(e.name_native().contains("Test Device"));
            }
        };
        m_config.defaultInitFactory = new BleDeviceConfig.InitTransactionFactory()
        {
            @Override public BleTransaction.Init newInitTxn()
            {
                return new BleTransaction.Init()
                {
                    @Override protected void start(BleDevice device)
                    {
                        device.read(mInitServiceUuid, mInitCharUuid, new BleDevice.ReadWriteListener()
                        {
                            @Override public void onEvent(ReadWriteEvent e)
                            {
                                succeed();
                            }
                        });
                    }
                };
            }
        };
        m_config.defaultAuthFactory = new BleDeviceConfig.AuthTransactionFactory()
        {
            @Override public BleTransaction.Auth newAuthTxn()
            {
                return new BleTransaction.Auth()
                {
                    @Override protected void start(BleDevice device)
                    {
                        device.read(mAuthServiceUuid, mAuthCharUuid, new BleDevice.ReadWriteListener()
                        {
                            @Override public void onEvent(ReadWriteEvent e)
                            {
                                succeed();
                            }
                        });
                    }
                };
            }
        };

        m_config.loggingEnabled = true;

        connectToMultipleDevices(m_config);

        m_mgr.stopScan();
        m_mgr.disconnectAll();

        m_config.runOnMainThread = true;

        connectToMultipleDevices(m_config);
    }

    private void connectToMultipleDevices(BleManagerConfig config) throws Exception
    {
        m_mgr.setConfig(config);

        final Semaphore s = new Semaphore(0);

        m_mgr.setListener_Discovery(new BleManager.DiscoveryListener()
        {
            final Pointer<Integer> connected = new Pointer(0);

            @Override public void onEvent(DiscoveryEvent e)
            {
                if (e.was(LifeCycle.DISCOVERED) || e.was(LifeCycle.REDISCOVERED))
                {
                    e.device().connect(new BleDevice.StateListener()
                    {
                        @Override public void onEvent(StateEvent e)
                        {
                            if (e.didEnter(BleDeviceState.INITIALIZED))
                            {
                                connected.value++;
                                System.out.println(e.device().getName_override() + " connected. #" + connected.value);
                                if (connected.value == 3)
                                {
                                    s.release();
                                }
                            }
                        }
                    });
                }
            }
        });

        m_mgr.setListener_State(new ManagerStateListener()
        {
            @Override public void onEvent(BleManager.StateListener.StateEvent e)
            {
                if (e.didEnter(BleManagerState.SCANNING))
                {
                    UnitTestUtils.advertiseNewDevice(m_mgr, -45, "Test Device #1");
                    UnitTestUtils.advertiseNewDevice(m_mgr, -35, "Test Device #2");
                    UnitTestUtils.advertiseNewDevice(m_mgr, -60, "Test Device #3");
                }
            }
        });

        m_mgr.startScan();
        s.acquire();
    }

    private class TransactionGattLayer extends UnitTestGatt
    {

        private final List<BluetoothGattService> mServices;


        public TransactionGattLayer(BleDevice device)
        {
            super(device);

            mServices = new ArrayList<>();

            BluetoothGattService authService = new BluetoothGattService(mAuthServiceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
            BluetoothGattCharacteristic authChar = new BluetoothGattCharacteristic(mAuthCharUuid, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
            authService.addCharacteristic(authChar);

            BluetoothGattService initService = new BluetoothGattService(mInitServiceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
            BluetoothGattCharacteristic initChar = new BluetoothGattCharacteristic(mInitCharUuid, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
            initService.addCharacteristic(initChar);

            mServices.add(authService);
            mServices.add(initService);
        }

        @Override public List<BluetoothGattService> getNativeServiceList(P_Logger logger)
        {
            return mServices;
        }

        @Override public BluetoothGattService getService(UUID serviceUuid, P_Logger logger)
        {
            for (BluetoothGattService s : mServices)
            {
                if (s.getUuid().equals(serviceUuid))
                {
                    return s;
                }
            }
            return null;
        }

        @Override public boolean readCharacteristic(BluetoothGattCharacteristic characteristic)
        {
            UnitTestUtils.readSuccess(getBleDevice(), characteristic, new byte[0]);
            return super.readCharacteristic(characteristic);
        }
    }

}
