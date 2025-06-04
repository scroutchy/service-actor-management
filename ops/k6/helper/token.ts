import { check } from 'k6';
import http from 'k6/http';
import { paths } from './url.ts';
import { clientUserParams } from './client-params.ts';

export function getAccessToken() {

    const authRes = http.post(paths.keycloakToken, clientUserParams, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    });

    check(authRes, {
        'Keycloak token obtained': (r) => r.status === 200 && r.json('access_token') !== undefined,
    });

    if (authRes.status === 200) {
        const accessToken = authRes.json('access_token')?.toString();
        return { accessToken };
    } else {
        console.error(`Failed to obtain access token: ${authRes.body}`);
        return null;
    }
}
