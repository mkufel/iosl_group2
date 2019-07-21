package visualization;

import java.io.IOException;

/**
 * Listener to call on simulation reload.
 */
public interface OnReloadListener {
    void onReload(String fileName) throws IOException;
}
