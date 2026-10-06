import 'dart:convert';

import 'package:basa_kita/api/auth.dart';
import 'package:basa_kita/api/client.dart';
import 'package:basa_kita/state/models.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  tearDown(() => api = ApiClient()); // back to demo mode

  test('with no API_URL the app stays in demo mode', () async {
    expect(api.connected, isFalse);
    final user = await AuthApi.signIn('admin@basakita.ph', 'x');
    expect(user.role, 'admin');
  });

  test('sign in calls the server and reads the user', () async {
    late http.Request seen;
    api = ApiClient(
      baseUrl: 'https://example.test/api/',
      client: MockClient((req) async {
        seen = req;
        return http.Response(
          jsonEncode({'user': {'firstName': 'Ana', 'lastName': 'Reyes', 'username': 'ana', 'email': 'ana@x.ph', 'role': 'admin'}}),
          200,
        );
      }),
    );
    final user = await AuthApi.signIn(' Ana@X.ph ', 'Secret123');
    expect(seen.url.toString(), 'https://example.test/api/auth/login');
    expect(jsonDecode(seen.body), {'email': 'ana@x.ph', 'password': 'Secret123'});
    expect(user.role, 'admin');
    expect(user.firstName, 'Ana');
  });

  test('the server message is shown as is', () async {
    api = ApiClient(
      baseUrl: 'https://example.test',
      client: MockClient((_) async => http.Response(jsonEncode({'message': 'Wrong email or password.'}), 401)),
    );
    await expectLater(AuthApi.signIn('a@b.ph', 'x'), throwsA('Wrong email or password.'));
  });

  test('a server error without a message gets a friendly default', () async {
    api = ApiClient(baseUrl: 'https://example.test', client: MockClient((_) async => http.Response('oops', 500)));
    await expectLater(AuthApi.requestPasswordReset('a@b.ph'), throwsA('Something went wrong. Please try again.'));
  });

  test('no internet gives a friendly message', () async {
    api = ApiClient(baseUrl: 'https://example.test', client: MockClient((_) async => throw http.ClientException('down')));
    await expectLater(AuthApi.resendCode('a@b.ph'), throwsA('Could not reach the server. Check your internet and try again.'));
  });

  test('sign up, verify and reset send the shapes in docs/BACKEND.md', () async {
    final calls = <String, Map<String, dynamic>>{};
    api = ApiClient(
      baseUrl: 'https://example.test',
      client: MockClient((req) async {
        calls[req.url.path] = jsonDecode(req.body) as Map<String, dynamic>;
        return http.Response(
          jsonEncode({'user': {'firstName': 'Sam', 'username': 'sam', 'email': 'sam@x.ph', 'role': 'pending'}}),
          200,
        );
      }),
    );
    final user = await AuthApi.signUp(name: 'Sam D', username: 'sam', email: 'sam@x.ph', password: 'Passw0rd');
    expect(user.role, 'pending');
    await AuthApi.verifyEmail(User(firstName: 'Sam', username: 'sam', email: 'sam@x.ph', role: 'pending'), '123456');
    await AuthApi.resetPassword('tok', 'Passw0rd');
    expect(calls['/auth/signup'], {'name': 'Sam D', 'username': 'sam', 'email': 'sam@x.ph', 'password': 'Passw0rd'});
    expect(calls['/auth/verify'], {'email': 'sam@x.ph', 'code': '123456'});
    expect(calls['/auth/reset-password'], {'token': 'tok', 'password': 'Passw0rd'});
  });
}
