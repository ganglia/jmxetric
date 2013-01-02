package ganglia.xdr.v31x;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

public class Ganglia_uuid implements XdrAble {
    public UUID uuid;

    public Ganglia_uuid() {
    }

    public Ganglia_uuid(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }
    
    protected byte[] getUUIDBytes() {
    	Long msb = uuid.getMostSignificantBits();
    	Long lsb = uuid.getLeastSignificantBits();
    	byte[] buf = new byte[16];
    	ByteBuffer _b = ByteBuffer.wrap(buf);
    	_b.order(ByteOrder.BIG_ENDIAN);
    	_b.putLong(msb);
    	_b.putLong(lsb);
    	return buf;
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeByteFixedVector(getUUIDBytes(), 16);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
    	uuid = getUUIDFromBytes(xdr.xdrDecodeByteFixedVector(16));
    }

	private UUID getUUIDFromBytes(byte[] buf) {
		ByteBuffer _b = ByteBuffer.wrap(buf);
		_b.order(ByteOrder.BIG_ENDIAN);
		Long msb = _b.getLong();
		Long lsb = _b.getLong();
		return new UUID(msb, lsb);
	}

}
