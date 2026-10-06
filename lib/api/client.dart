import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

/// One place for talking to the backend, like `src/api/client.js` on the website.
///
/// Set the server address when you build or run the app:
///
///     flutter run --dart-define=API_URL=https://yourdomain/api
///
/// While it is empty the app runs in demo mode: nothing leaves the phone.
const _apiUrl = String.fromEnvironment('API_URL');

class ApiClient {
  final String baseUrl;
  final http.Client _client;

  ApiClient({String? baseUrl, http.Client? client})
      : baseUrl = (baseUrl ?? _apiUrl).replaceAll(RegExp(r'/+$'), ''),
        _client = client ?? http.Client();

  bool get connected => baseUrl.isNotEmpty;

  /// JSON POST. Throws a plain message that is safe to show on screen: the server's own
  /// `message` when it sends one, otherwise a friendly default.
  Future<Map<String, dynamic>> post(String path, Map<String, dynamic> body) async {
    final http.Response res;
    try {
      res = await _client
          .post(
            Uri.parse('$baseUrl$path'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode(body),
          )
          .timeout(const Duration(seconds: 15));
    } on Object {
      throw 'Could not reach the server. Check your internet and try again.';
    }
    Map<String, dynamic> data = {};
    try {
      final decoded = jsonDecode(utf8.decode(res.bodyBytes));
      if (decoded is Map<String, dynamic>) data = decoded;
    } on FormatException {
      // an empty or non-JSON body is fine for calls that return {}
    }
    if (res.statusCode < 200 || res.statusCode >= 300) {
      throw data['message'] as String? ?? 'Something went wrong. Please try again.';
    }
    return data;
  }
}

/// The client the app uses. Replaced in tests.
ApiClient api = ApiClient();
