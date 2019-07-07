package visualization;

import java.io.IOException;

/**
 * Callback to call on simulation reload.
 */
public interface OnReloadListener {
    void onReload() throws IOException;
}