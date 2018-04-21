package no.nordicsemi.android.ble.common.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataCallback;
import no.nordicsemi.android.ble.common.util.CRC16;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class ContinuousGlucoseMeasurementDataCallbackTest {

	@Test
	public void onContinuousGlucoseMeasurementReceived_full() {
		final DataCallback callback = new ContinuousGlucoseMeasurementDataCallback() {
			@Override
			public void onContinuousGlucoseMeasurementReceived(final float glucoseConcentration, @Nullable final Float cgmTrend, @Nullable final Float cgmQuality, final int timeOffset) {
				assertEquals("Glucose", 12.34, glucoseConcentration, 0.01);
				assertNotNull("Trend present", cgmTrend);
				assertEquals("Trend", 0.2f, cgmTrend, 0.01);
				assertNotNull("Quality present", cgmQuality);
				assertEquals("Quality", 99.7f, cgmQuality, 0.01);
				assertEquals("Time offset", 5, timeOffset);
			}

			@Override
			public void onSensorStatusChanged(final Status status, final int timeOffset) {
				assertNotNull("Status present", status);
			}

			@Override
			public void onCrcError(@NonNull final Data data) {
				final int expectedCrc = data.getIntValue(Data.FORMAT_UINT16, 13);
				final int actualCrc   = CRC16.MCRF4XX(data.getValue(), 0, 13);
				assertEquals("CRC error on valid data", expectedCrc, actualCrc);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final Data data = new Data(new byte[15]);
		// Size
		assertTrue(data.setValue(15, Data.FORMAT_UINT8, 0));
		// Flags
		assertTrue(data.setByte(0b11100011, 1));
		// Glucose Concentration
		assertTrue(data.setValue(1234, -2, Data.FORMAT_SFLOAT, 2));
		// Time offset
		assertTrue(data.setValue(5, Data.FORMAT_UINT16, 4));
		// Status
		assertTrue(data.setByte(0x02, 6)); // Warning status: Low battery
		assertTrue(data.setByte(0x14, 7)); // Cal/Temp status: Sensor Temperature too high / Calibration recommended
		assertTrue(data.setByte(0xFF, 8)); // Sensor status: All fields
		// Trend
		assertTrue(data.setValue(2, -1, Data.FORMAT_SFLOAT, 9));
		// Quality
		assertTrue(data.setValue(997, -1, Data.FORMAT_SFLOAT, 11));
		// E2E CRC
		assertTrue(data.setValue(0x3F8E, Data.FORMAT_UINT16, 13));

		callback.onDataReceived(null, data);
	}

	@Test
	public void onContinuousGlucoseMeasurementReceived_small() {
		final DataCallback callback = new ContinuousGlucoseMeasurementDataCallback() {
			@Override
			public void onContinuousGlucoseMeasurementReceived(final float glucoseConcentration, @Nullable final Float cgmTrend, @Nullable final Float cgmQuality, final int timeOffset) {
				assertEquals("Glucose", 120.0, glucoseConcentration, 0.01);
				assertNull("Trend not present", cgmTrend);
				assertNull("Quality not present", cgmQuality);
				assertEquals("Time offset", 6, timeOffset);
			}

			@Override
			public void onSensorStatusChanged(final Status status, final int timeOffset) {
				assertEquals("Status not present but reported", 1, 2);
			}

			@Override
			public void onCrcError(@NonNull final Data data) {
				assertEquals("CRC error on valid data", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final Data data = new Data(new byte[6]);
		// Size
		assertTrue(data.setValue(6, Data.FORMAT_UINT8, 0));
		// Flags
		assertTrue(data.setByte(0b00000000, 1));
		// Glucose Concentration
		assertTrue(data.setValue(12, 1, Data.FORMAT_SFLOAT, 2));
		// Time offset
		assertTrue(data.setValue(6, Data.FORMAT_UINT16, 4));

		callback.onDataReceived(null, data);
	}

	@Test
	public void onContinuousGlucoseMeasurementReceived_double() {
		final DataCallback callback = new ContinuousGlucoseMeasurementDataCallback() {
			private int time = 5;

			@Override
			public void onContinuousGlucoseMeasurementReceived(final float glucoseConcentration, @Nullable final Float cgmTrend, @Nullable final Float cgmQuality, final int timeOffset) {
				assertEquals("Glucose", 120.0, glucoseConcentration, 0.01);
				assertNull("Trend not present", cgmTrend);
				assertNull("Quality not present", cgmQuality);
				assertEquals("Time offset", time++, timeOffset);
			}

			@Override
			public void onSensorStatusChanged(final Status status, final int timeOffset) {
				assertEquals("Status not present but reported", 1, 2);
			}

			@Override
			public void onCrcError(@NonNull final Data data) {
				assertEquals("CRC error on valid data", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final Data data = new Data(new byte[12]);
		// Size
		assertTrue(data.setValue(6, Data.FORMAT_UINT8, 0));
		// Flags
		assertTrue(data.setByte(0b00000000, 1));
		// Glucose Concentration
		assertTrue(data.setValue(12, 1, Data.FORMAT_SFLOAT, 2));
		// Time offset
		assertTrue(data.setValue(5, Data.FORMAT_UINT16, 4));

		// Size
		assertTrue(data.setValue(6, Data.FORMAT_UINT8, 6));
		// Flags
		assertTrue(data.setByte(0b00000000, 7));
		// Glucose Concentration
		assertTrue(data.setValue(12, 1, Data.FORMAT_SFLOAT, 8));
		// Time offset
		assertTrue(data.setValue(6, Data.FORMAT_UINT16, 10));

		callback.onDataReceived(null, data);
	}

	@Test
	public void onContinuousGlucoseMeasurementReceived_crcError() {
		final DataCallback callback = new ContinuousGlucoseMeasurementDataCallback() {
			@Override
			public void onContinuousGlucoseMeasurementReceived(final float glucoseConcentration, @Nullable final Float cgmTrend, @Nullable final Float cgmQuality, final int timeOffset) {
				assertEquals("Measurement reported despite wrong CRC", 1, 2);
			}

			@Override
			public void onSensorStatusChanged(final Status status, final int timeOffset) {
				assertEquals("Status reported despite wrong CRC", 1, 2);
			}

			@Override
			public void onCrcError(@NonNull final Data data) {
				final int actualCrc = CRC16.MCRF4XX(data.getValue(), 0, 6);
				assertEquals("CRC error", 0x6F59, actualCrc);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final Data data = new Data(new byte[8]);
		// Size
		assertTrue(data.setValue(8, Data.FORMAT_UINT8, 0));
		// Flags
		assertTrue(data.setByte(0b00000000, 1));
		// Glucose Concentration
		assertTrue(data.setValue(12, 1, Data.FORMAT_SFLOAT, 2));
		// Time offset
		assertTrue(data.setValue(6, Data.FORMAT_UINT16, 4));
		// E2E CRC
		assertTrue(data.setValue(0x6F58, Data.FORMAT_UINT16, 6));

		callback.onDataReceived(null, data);
	}

	@Test
	public void onInvalidDataReceived_tooShort() {
		final DataCallback callback = new ContinuousGlucoseMeasurementDataCallback() {
			@Override
			public void onContinuousGlucoseMeasurementReceived(final float glucoseConcentration, @Nullable final Float cgmTrend, @Nullable final Float cgmQuality, final int timeOffset) {
				assertEquals("Measurement reported despite invalid data", 1, 2);
			}

			@Override
			public void onSensorStatusChanged(final Status status, final int timeOffset) {
				assertEquals("Status reported despite invalid data", 1, 2);
			}

			@Override
			public void onCrcError(@NonNull final Data data) {
				assertEquals("Invalid data reported as CRC error", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid data", 1, 1);
			}
		};
		final Data data = new Data(new byte[5]);
		// Size
		assertTrue(data.setValue(6, Data.FORMAT_UINT8, 0));
		// Flags
		assertTrue(data.setByte(0b11100011, 1));
		// Glucose Concentration
		assertTrue(data.setValue(12, 1, Data.FORMAT_SFLOAT, 2));
		// Time offset
		assertFalse(data.setValue(6, Data.FORMAT_UINT16, 4));

		callback.onDataReceived(null, data);
	}

	@Test
	public void onInvalidDataReceived() {
		final DataCallback callback = new ContinuousGlucoseMeasurementDataCallback() {
			@Override
			public void onContinuousGlucoseMeasurementReceived(final float glucoseConcentration, @Nullable final Float cgmTrend, @Nullable final Float cgmQuality, final int timeOffset) {
				assertEquals("Measurement reported despite invalid data", 1, 2);
			}

			@Override
			public void onSensorStatusChanged(final Status status, final int timeOffset) {
				assertEquals("Status reported despite invalid data", 1, 2);
			}

			@Override
			public void onCrcError(@NonNull final Data data) {
				assertEquals("Invalid data reported as CRC error", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid data", 1, 1);
			}
		};
		final Data data = new Data(new byte[7]);
		// Size
		assertTrue(data.setValue(7, Data.FORMAT_UINT8, 0));
		// Flags
		assertTrue(data.setByte(0b11100011, 1));
		// Glucose Concentration
		assertTrue(data.setValue(12, 1, Data.FORMAT_SFLOAT, 2));
		// Time offset
		assertTrue(data.setValue(6, Data.FORMAT_UINT16, 4));
		// Trend
		assertFalse(data.setValue(2, -1, Data.FORMAT_SFLOAT, 9));

		callback.onDataReceived(null, data);
	}
}